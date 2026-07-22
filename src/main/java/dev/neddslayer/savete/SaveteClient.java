package dev.neddslayer.savete;

import dev.neddslayer.savete.gameplay.LocalPlayerManager;
import dev.neddslayer.savete.gameplay.movement.GrappleController;
import dev.neddslayer.savete.gameplay.upgrade.api.AbstractUpgrade;
import dev.neddslayer.savete.gameplay.upgrade.UpgradeRegistry;
import dev.neddslayer.savete.gameplay.upgrade.UpgradeType;
import dev.neddslayer.savete.registrar.BlockEntityRegistrar;
import dev.neddslayer.savete.registrar.EntityRegistrar;
import dev.neddslayer.savete.registrar.ItemRegistrar;
import dev.neddslayer.savete.render.UpgradeModel;
import dev.neddslayer.savete.render.block.FinishLevelBlockRenderer;
import dev.neddslayer.savete.render.block.LateBlockEntityRenderer;
import dev.neddslayer.savete.render.block.NextLevelInfoRenderer;
import dev.neddslayer.savete.render.entity.*;
import dev.neddslayer.savete.render.entity.model.SquelchModel;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.api.client.render.post.PostProcessingManager;
import foundry.veil.platform.VeilEventPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;

import java.util.*;

import static dev.neddslayer.savete.render.entity.UpgradeHolderRenderer.UPGRADE_LOCATION;
import static java.lang.Math.abs;
import static org.lwjgl.opengl.GL11.*;

@Mod(value = Savete.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Savete.MODID, value = Dist.CLIENT)
public class SaveteClient<T extends Entity> {

    public Map<T, LateEntityRenderer<T>> LATE_ENTITY_QUEUE = new HashMap<>();
    public List<BlockEntity> BLOCK_ENTITY_QUEUE = new ArrayList<>();
    public static SaveteClient INSTANCE;
    public static float distanceToFinish = 99999;
    private static float lerpedDistance = 5;
    public static boolean forceFullOverlay = false;

    private static int compareEntityDistances(Entity a, Entity b, Vec3 position) {
        if (a.distanceToSqr(position) == b.distanceToSqr(position)) return 0;

        return a.distanceToSqr(position) > b.distanceToSqr(position) ? -1 : 1;
    }

    public SaveteClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        INSTANCE = this;

        VeilEventPlatform.INSTANCE.onVeilRenderLevelStage((stage, levelRenderer, bufferSource, matrixStack, frustumMatrix, projectionMatrix, renderTick, deltaTracker, camera, frustum) -> {
            PostProcessingManager postManager = VeilRenderSystem.renderer().getPostProcessingManager();
            LocalPlayer player = Minecraft.getInstance().player;
            float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(false);
            switch (stage) {
                case AFTER_SKY -> {
                    if (Minecraft.getInstance().level.dimension().location().getPath().equals("void_tunnels")) {
                        PostPipeline pipeline = postManager.getPipeline(Savete.path("skybox"));
                        postManager.runPipeline(pipeline);
                    }
                }
                case AFTER_PARTICLES -> {
                    AdvancedFbo.getMainFramebuffer().resolveToAdvancedFbo(
                            VeilRenderSystem.renderer().getFramebufferManager().getFramebuffer(Savete.path("rift")),
                            GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT,
                            GL_NEAREST);
                    BLOCK_ENTITY_QUEUE.forEach(be -> {
                        matrixStack.matrixPush();
                        matrixStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
                        matrixStack.translate(be.getBlockPos().getCenter().x, be.getBlockPos().getCenter().y, be.getBlockPos().getCenter().z);
                        BlockEntityRenderer<? extends BlockEntity> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(be);
                        if (renderer instanceof LateBlockEntityRenderer<? extends BlockEntity> late) {
                            Level level = be.getLevel();
                            int i;
                            if (level != null) {
                                i = LevelRenderer.getLightColor(level, be.getBlockPos());
                            } else {
                                i = 15728880;
                            }
                            late.renderLate(be, partialTicks, matrixStack.toPoseStack(), bufferSource, i, OverlayTexture.NO_OVERLAY);
                        }
                        matrixStack.matrixPop();
                    });
                    BLOCK_ENTITY_QUEUE.clear();
                    LATE_ENTITY_QUEUE.entrySet().stream().sorted((a, b) -> compareEntityDistances(a.getKey(), b.getKey(), camera.getPosition())).forEach((entry) -> {
                        T entity = entry.getKey();
                        LateEntityRenderer<T> renderer = entry.getValue();
                        double d0 = Mth.lerp(partialTicks, entity.xOld, entity.getX());
                        double d1 = Mth.lerp(partialTicks, entity.yOld, entity.getY());
                        double d2 = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
                        float f = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
                        Vec3 vec3 = renderer.getRenderOffset(entity, partialTicks);
                        double offX = d0 + vec3.x();
                        double offY = d1 + vec3.y();
                        double offZ = d2 + vec3.z();
                        matrixStack.matrixPush();
                        matrixStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
                        matrixStack.translate(offX, offY, offZ);
                        renderer.renderLate(entity, f, partialTicks, matrixStack.toPoseStack(), bufferSource, renderer.getPackedLightCoords(entity, partialTicks));
                        matrixStack.matrixPop();
                    });
                    LATE_ENTITY_QUEUE.clear();
                }
                case AFTER_LEVEL -> {
                    if (!player.getInventory().getArmor(3).isEmpty() && player.getInventory().getArmor(3).is(ItemRegistrar.VOID_SUIT_HELMET.get())) {
                        PostPipeline pipeline = postManager.getPipeline(Savete.path("visor"));
                        postManager.runPipeline(pipeline);
                    }

                    if (forceFullOverlay) distanceToFinish = 0;

                    if (distanceToFinish < 5) {
                        lerpedDistance = Mth.lerp(deltaTracker.getRealtimeDeltaTicks() * 0.5f, lerpedDistance, distanceToFinish);
                    } else {
                        lerpedDistance = Mth.lerp(deltaTracker.getRealtimeDeltaTicks() * 0.5f, lerpedDistance, 5);
                    }

                    if (lerpedDistance < 5) {
                        PostPipeline pipeline = postManager.getPipeline(Savete.path("waiting_screen"));
                        pipeline.getUniformSafe("Fill").setFloat(lerpedDistance / 5);
                        postManager.runPipeline(pipeline, true);
                    }
                }
                default -> {}
            }
        });
    }

    @SubscribeEvent
    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistrar.RIFT.get(), RiftEntityRenderer::new);
        event.registerEntityRenderer(EntityRegistrar.GRAPPLE_POINT.get(), GrapplePointRenderer::new);
        event.registerEntityRenderer(EntityRegistrar.BLOCKER.get(), SaveteBlockerRenderer::new);
        event.registerEntityRenderer(EntityRegistrar.UPGRADE_HOLDER.get(), UpgradeHolderRenderer::new);

        event.registerEntityRenderer(EntityRegistrar.SQUELCH.get(), SquelchEntityRenderer::new);
        event.registerEntityRenderer(EntityRegistrar.HELIOS.get(), HeliosEntityRenderer::new);
        event.registerEntityRenderer(EntityRegistrar.TANTRUM.get(), TantrumEntityRenderer::new);
        event.registerEntityRenderer(EntityRegistrar.HALO.get(), HaloEntityRenderer::new);
        event.registerEntityRenderer(EntityRegistrar.ORBIT.get(), OrbitEntityRenderer::new);

        event.registerBlockEntityRenderer(BlockEntityRegistrar.FINISH_LEVEL.get(), FinishLevelBlockRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistrar.NEXT_LEVEL_INFO.get(), NextLevelInfoRenderer::new);
    }

    @SubscribeEvent
    private static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SquelchModel.LAYER_LOCATION, SquelchModel::createBodyLayer);
    }


    @SubscribeEvent
    private static void entityEquip(LivingEquipmentChangeEvent event) {
        if (Minecraft.getInstance().player != null && event.getTo().is(ItemRegistrar.VOID_SUIT_HELMET.get()) && event.getEntity().getUUID().equals(Minecraft.getInstance().player.getUUID())) {
            LocalPlayerManager.setPlayerController(GrappleController.INSTANCE);
        }
    }

    @SubscribeEvent
    private static void onBake(ModelEvent.RegisterAdditional event) {
        List<? extends UpgradeType<? extends AbstractUpgrade>> entries = UpgradeRegistry.REGISTRY.getRegistry().get().entrySet().stream().map(Map.Entry::getValue).toList();
        entries.forEach(u -> event.register(new ModelResourceLocation(u.getTextureLocation().withPath("upgrade/" + u.getTextureLocation().getPath()), "standalone")));
    }

    @SubscribeEvent
    private static void onBake(ModelEvent.RegisterGeometryLoaders event) {
        event.register(Savete.path("upgrade_loader"), UpgradeModel.Loader.INSTANCE);
    }

    @SubscribeEvent
    private static void onRegisterAtlases(RegisterMaterialAtlasesEvent event) {
        event.register(UPGRADE_LOCATION, Savete.path("upgrades"));
    }

    @SubscribeEvent
    private static void onTickPre(ClientTickEvent.Pre event) {
        distanceToFinish = Float.MAX_VALUE;
    }
}
