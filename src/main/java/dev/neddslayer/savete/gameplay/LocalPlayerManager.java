package dev.neddslayer.savete.gameplay;

import com.mojang.blaze3d.platform.Window;
import dev.neddslayer.savete.entity.IVoidTooltipHolder;
import dev.neddslayer.savete.gameplay.movement.IPlayerController;
import dev.neddslayer.savete.gameplay.upgrade.api.IClientUpgrade;
import dev.neddslayer.savete.gameplay.upgrade.UpgradeType;
import dev.neddslayer.savete.registrar.ItemRegistrar;
import dev.neddslayer.savete.render.GuiHelper;
import foundry.veil.api.client.render.CameraMatrices;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.DirectionalLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

@EventBusSubscriber(Dist.CLIENT)
public class LocalPlayerManager {
    private static IPlayerController controller;

    private static LightRenderHandle<DirectionalLightData> directionalLight;
    private static int rotateTicks = 0;

    private static float currentGemstoneCount = 0;
    public static float gemstoneCount = 0;
    public static int rawGemsBroken;
    public static int totalRawGems;
    public static int currentLevel;
    public static boolean inIntermission;

    public static final List<IClientUpgrade> clientUpgrades = new ArrayList<>();
    public static List<UpgradeType<?>> currentUpgrades = new ArrayList<>();
    public static List<EntityType<?>> predictedEnemies = new ArrayList<>();

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    private static void renderLevel(RenderLevelStageEvent event) {
        float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            if (controller != null) controller.renderWorld(VeilRenderBridge.create(event.getPoseStack()), event.getCamera(), Minecraft.getInstance().renderBuffers().bufferSource(), partialTicks);

            if (directionalLight != null) {
                Vector3f direction = new Vector3f(1).rotateX((rotateTicks + partialTicks) * Mth.DEG_TO_RAD).rotateY((rotateTicks + partialTicks) * Mth.DEG_TO_RAD).rotateZ((rotateTicks + partialTicks) * Mth.DEG_TO_RAD).normalize();
                directionalLight.getLightData().setDirection(direction);
                directionalLight.getLightData().setColor(0.3294f, 0.3294f, 1.0f);
                directionalLight.getLightData().setBrightness(0.2f);
            }
        }
    }

    @SubscribeEvent
    private static void onClick(InputEvent.MouseButton.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (controller != null && Minecraft.getInstance().screen == null && player != null && player.isAlive()) {
            controller.onClick(event);
            if (event.getAction() == GLFW_PRESS) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    private static void clientTick(ClientTickEvent.Pre event) {
        if (Minecraft.getInstance().isPaused()) return;
        if (controller != null) controller.tick();
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            if (GameController.isVoidTunnels(level)) {
                if (directionalLight == null) {
                    directionalLight = VeilRenderSystem.renderer().getLightRenderer().addLight(new DirectionalLightData().setBrightness(0.05f));
                }
                rotateTicks++;

                for (IClientUpgrade upgrade : clientUpgrades) {
                    upgrade.clientTick();
                }

            } else {
                if (directionalLight != null) {
                    directionalLight.free();
                    directionalLight = null;
                }
                gemstoneCount = 0;
                currentGemstoneCount = 0;
            }
        }
        currentGemstoneCount = Mth.lerp(0.25f, currentGemstoneCount, gemstoneCount);
    }

    @SubscribeEvent
    private static void renderHud(RenderGuiEvent.Post event) {
        GuiGraphics graphics = event.getGuiGraphics();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        if (controller != null) controller.renderHud(graphics, partialTick);
        if (GameController.isVoidTunnels(Minecraft.getInstance().level)) {
            Font font = Minecraft.getInstance().font;
            Component text = Component.literal(String.format("%.2f", currentGemstoneCount));
            graphics.pose().pushPose();

            graphics.pose().translate(graphics.guiWidth() - 1.5f * font.width(text) - 40, 40, 0);
            graphics.pose().scale(1.5f, 1.5f, 1.5f);

            graphics.drawString(font, text, 0, 0, 0xFFFF00FF);

            graphics.pose().popPose();

            graphics.drawCenteredString(font, "Level " + currentLevel, graphics.guiWidth() / 2, 10, 0xFFFFFFFF);
            if (inIntermission) {
                int x = (graphics.guiWidth() / 2) - font.width("INTERMISSION") / 2;
                GuiHelper.drawWobblyText(graphics, font, Component.literal("INTERMISSION"), x, 20, 0xFFFFFFFF, partialTick, Minecraft.getInstance().levelRenderer.getTicks() + 30, 1, 0.15, 0.3);
            }

            for (IClientUpgrade upgrade : clientUpgrades) {
                upgrade.renderHud(graphics, partialTick);
            }

            int height = graphics.guiHeight() - 20;
            Object2IntMap<UpgradeType<?>> map = new Object2IntArrayMap<>();

            for (UpgradeType<?> type : currentUpgrades) {
                map.computeInt(type, (t, i) -> {if (i == null) return 1; return i + 1;});
            }

            for (UpgradeType<?> type : map.keySet()) {
                MutableComponent name = Component.translatable(GuiHelper.upgradeLocationToTranslatable(type.getTextureLocation()) + ".name");
                if (map.getOrDefault(type, 0) > 1) {
                    name.append(Component.literal(" x" + map.getOrDefault(type, 0)).withStyle(ChatFormatting.GRAY));
                }
                GuiHelper.drawWobblyText(graphics, font, name, 10, height, 0xFFFFFFFF, partialTick, Minecraft.getInstance().levelRenderer.getTicks() + height / 2, 0.5, 0.05, 0.35);
                height -= 10;
            }
        }

        Entity pick = Minecraft.getInstance().crosshairPickEntity;
        if (pick instanceof IVoidTooltipHolder tooltipHolder && Minecraft.getInstance().player != null && Minecraft.getInstance().player.getInventory().getArmor(3).is(ItemRegistrar.VOID_SUIT_HELMET)) {
            CameraMatrices matrices = VeilRenderSystem.renderer().getCameraMatrices();
            Vec3 pos = pick.getPosition(partialTick).add(tooltipHolder.tooltipPositionOffset()).subtract(new Vec3(matrices.getCameraPosition()));
            Window window = Minecraft.getInstance().getWindow();

            Matrix4f viewProjMat = matrices.getProjectionMatrix().mul(matrices.getViewMatrix(), new Matrix4f());
            Vector4f screenCoordinates = viewProjMat.transform(new Vector4f(pos.toVector3f(), 1.0f));
            screenCoordinates.x = (screenCoordinates.x / screenCoordinates.w + 1) * 0.5f * window.getGuiScaledWidth();
            screenCoordinates.y = (1.0f - (screenCoordinates.y / screenCoordinates.w + 1) * 0.5f) * window.getGuiScaledHeight();

            tooltipHolder.renderTooltip(graphics, Math.round(screenCoordinates.x), Math.round(screenCoordinates.y));
        }
    }

    @SubscribeEvent
    private static void renderHand(RenderHandEvent event) {
        if (controller != null) controller.renderHand(event.getHand(), VeilRenderBridge.create(event.getPoseStack()), event.getMultiBufferSource(), event.getPackedLight(), event.getPartialTick(), event.getInterpolatedPitch(), event.getSwingProgress(), event.getEquipProgress(), event.getItemStack());
    }

    public static void setPlayerController(@Nullable IPlayerController newController) {
        if (controller == newController) return;
        if (controller != null) controller.onDeEquip();
        controller = newController;
        if (newController != null) newController.onEquip();
    }

    public static float calculateCost(UpgradeType<?> type) {
        int count = (int) currentUpgrades.stream().filter(t -> t == type).count();
        return Math.round(2 * type.getGemCost() * (float)Math.pow(type.getStackMultiplier(), count)) / 2.0f;
    }
}
