package dev.neddslayer.savete.gameplay.movement;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.entity.GrapplePointEntity;
import dev.neddslayer.savete.network.ForceServerPlayerDeltaMovement;
import dev.neddslayer.savete.network.GrappleUpdatePacket;
import foundry.veil.api.client.render.MatrixStack;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.*;
import org.lwjgl.glfw.GLFW;

import java.lang.Math;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.Math.sqrt;

@EventBusSubscriber(Dist.CLIENT)
public class GrappleController implements IPlayerController {
    public static final GrappleController INSTANCE = new GrappleController();

    private GrappleController() {}

    public static final Map<UUID, Vec3> GRAPPLING_PLAYERS = new HashMap<>();

    public Vec3 hookLocation = null;
    public int hookTicks = 0;
    public int hookCooldown = 100;
    private int hookDebounce = 0;
    public boolean reeling = false, forceReeling = false;
    public double ropeLength = 0;

    public void onEquip() {
        cancelGrapple();
    }

    public void onDeEquip() {
        cancelGrapple();
    }

    public void onClick(InputEvent.MouseButton event) {
        LocalPlayer player = Minecraft.getInstance().player;
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        float partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_2 && player != null) {
            if (event.getAction() == GLFW.GLFW_PRESS && hookDebounce <= 0) {
                reeling = false;
                hookDebounce = 4;
                HitResult result;
                if (hookCooldown >= 5) {
                    result = ProjectileUtil.getEntityHitResult(Minecraft.getInstance().level, player, camera.getPosition(),
                            camera.getPosition().add(new Vec3(camera.getLookVector().mul(20, new Vector3f()))), AABB.ofSize(camera.getPosition(), 40, 40, 40), e -> e instanceof GrapplePointEntity);
                    if (result != null) {
                        hookLocation = result.getLocation();
                        hookCooldown -= 5;
                        forceReeling = true;
                        player.setDeltaMovement(Vec3.ZERO);
                        PacketDistributor.sendToServer(new GrappleUpdatePacket(player.getUUID(), hookLocation.x, hookLocation.y, hookLocation.z));
                        return;
                    }
                }

                ClipContext context = new ClipContext(
                        camera.getPosition(),
                        camera.getPosition().add(new Vec3(camera.getLookVector().mul(20, new Vector3f()))),
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        CollisionContext.empty()
                );
                result = Minecraft.getInstance().level.clip(context);
                if (result.getType() != HitResult.Type.MISS && hookLocation == null && hookCooldown > 5) {
                    hookLocation = result.getLocation();
                    ropeLength = player.getPosition(partialTicks).distanceTo(hookLocation);
                    hookTicks = 0;
                    hookCooldown -= 5;
                    forceReeling = false;
                    PacketDistributor.sendToServer(new GrappleUpdatePacket(player.getUUID(), hookLocation.x, hookLocation.y, hookLocation.z));
                } else if (hookLocation == null && hookCooldown > 5) {
                    for (float i = -1f; i < 1f; i += 0.25f) {
                        for (float j = -1; j < 1f; j += 0.25f) {
                            for (float k = -1f; k < 1f; k += 0.25f) {
                                ClipContext context2 = new ClipContext(
                                        camera.getPosition(),
                                        camera.getPosition().add(new Vec3(camera.getLookVector().mul(20, new Vector3f()))).add(i, j, k),
                                        ClipContext.Block.COLLIDER,
                                        ClipContext.Fluid.NONE,
                                        CollisionContext.empty()
                                );
                                HitResult result2 = Minecraft.getInstance().level.clip(context2);
                                if (result2.getType() != HitResult.Type.MISS && hookLocation == null) {
                                    hookLocation = result2.getLocation();
                                    ropeLength = player.getPosition(partialTicks).distanceTo(hookLocation);
                                    hookTicks = 0;
                                    forceReeling = false;
                                    hookCooldown -= 5;
                                    PacketDistributor.sendToServer(new GrappleUpdatePacket(player.getUUID(), hookLocation.x, hookLocation.y, hookLocation.z));
                                    break;
                                }
                            }
                        }
                    }
                }
            } else if (hookLocation != null) {
                Vec3 motion = player.getDeltaMovement().multiply(1.05 + 0.75 - (hookCooldown / 100.0) * 0.75, 1.1, 1.05 + 0.75 - (hookCooldown / 100.0) * 0.75);
                player.setDeltaMovement(motion.x, motion.y, motion.z);
                cancelGrapple();
            }
        }
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_1 && player != null) {
            if (event.getAction() == GLFW.GLFW_PRESS && hookLocation != null) {
                reeling = true;
                player.setDeltaMovement(Vec3.ZERO);
            } else if (hookLocation != null) {
                reeling = false;
                ropeLength = player.getPosition(partialTicks).distanceTo(hookLocation);
            }
        }
    }

    public void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (hookLocation != null) {
            if (player != null && hookCooldown <= 0) {
                cancelGrapple();
                return;
            }
            if (hookTicks < 3) hookTicks++;
            hookCooldown -= 2;
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            float partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);

            Vec3 motion = player.getDeltaMovement();

            double currentDistance = player.getPosition(partialTicks).distanceTo(hookLocation);
            double remainingLength = ropeLength - currentDistance;

            if (!reeling && !forceReeling) {
                // air friction
                motion = motion.subtract(motion.multiply(0.01, 0.01, 0.01));

                // snap to rope length
                if (remainingLength <= 0) {
                    motion = motion.subtract(player.getPosition(partialTicks).subtract(hookLocation).normalize().multiply(sqrt(-remainingLength), sqrt(-remainingLength), sqrt(-remainingLength)).multiply(0.25f, 0.25f, 0.25f));
                }

                Vec3 playerMovement = new Vec3(camera.getLeftVector()).multiply(player.input.leftImpulse, player.input.leftImpulse, player.input.leftImpulse).add(new Vec3(camera.getLookVector()).multiply(player.input.forwardImpulse, player.input.forwardImpulse, player.input.forwardImpulse)).multiply(0.05, 0.05, 0.05);
                motion = motion.add(playerMovement);
            } else {
                double speed = forceReeling ? 1.25 : 0.75;
                motion = hookLocation.subtract(player.getPosition(partialTicks)).normalize().multiply(speed, speed, speed);
            }

            if (Double.isNaN(motion.x) || Double.isNaN(motion.y) || Double.isNaN(motion.z)) {
                motion = new Vec3(0, 0, 0);
                Savete.LOGGER.error("Grapple motion is NaN");
            }

            player.setDeltaMovement(motion.x, motion.y, motion.z);
            PacketDistributor.sendToServer(new ForceServerPlayerDeltaMovement(motion.toVector3f()));

            if (player.onGround()) {
                cancelGrapple();
            }
        } else {
            if (hookCooldown < 100) hookCooldown++;
            if (hookDebounce > 0) hookDebounce--;
        }
    }

    public void cancelGrapple() {
        LocalPlayer player = Minecraft.getInstance().player;
        hookLocation = null;
        PacketDistributor.sendToServer(new GrappleUpdatePacket(player.getUUID(), Double.NaN, Double.NaN, Double.NaN));
    }

    public void renderWorld(MatrixStack matrixStack, Camera camera, MultiBufferSource bufferSource, float partialTicks) {
        LocalPlayer player = Minecraft.getInstance().player;
        Level level = Minecraft.getInstance().level;
        VertexConsumer consumer = bufferSource.getBuffer(VeilRenderType.get(Savete.path("solid_line")));
        if (hookLocation != null) {
            matrixStack.matrixPush();
            matrixStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);

            Vec3 up = new Vec3(0, 1, 0);
            Vec3 leftVector = player.calculateViewVector(player.getViewXRot(partialTicks), player.getPreciseBodyRotation(partialTicks) + 90);
            Vec3 hookStart = player.getEyePosition(partialTicks).subtract(up.multiply(0.25, 0.25, 0.25)).add(leftVector.multiply(0.25, 0.25, 0.25));
            Vec3 hookEnd = hookStart.lerp(hookLocation, Math.clamp(hookTicks + partialTicks, 0, 3) / 3);
            Vector3f normal = hookEnd.subtract(hookStart).normalize().toVector3f();
            consumer.addVertex(matrixStack.pose(), hookStart.toVector3f()).setNormal(matrixStack.pose(), normal.x, normal.y, normal.z).setColor(1f, 1f, 1f, 1f);
            consumer.addVertex(matrixStack.pose(), hookEnd.toVector3f())  .setNormal(matrixStack.pose(), normal.x, normal.y, normal.z).setColor(1f, 1f, 1f, 1f);
            matrixStack.matrixPop();
        }


    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    private static void renderLevel(RenderLevelStageEvent event) {
        float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        MatrixStack matrixStack = VeilRenderBridge.create(event.getPoseStack());
        Camera camera = event.getCamera();
        Level level = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;
        VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(VeilRenderType.get(Savete.path("solid_line")));
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            // Render other player's hooks
            GRAPPLING_PLAYERS.forEach((k, v) -> {
                Player other = level.getPlayerByUUID(k);
                if (!Double.isNaN(v.x) && !Double.isNaN(v.y) && !Double.isNaN(v.z) && other != null && !k.equals(player.getUUID())) {
                    matrixStack.matrixPush();
                    matrixStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);

                    Vec3 up = new Vec3(0, 1, 0);
                    Vec3 leftVector = other.calculateViewVector(other.getViewXRot(partialTicks), other.getPreciseBodyRotation(partialTicks) + 90);
                    Vec3 hookStart = other.getEyePosition(partialTicks).subtract(up.multiply(0.25, 0.25, 0.25)).add(leftVector.multiply(0.25, 0.25, 0.25));
                    consumer.addVertex(matrixStack.pose(), hookStart.toVector3f()).setNormal(0, 1, 0).setColor(1f, 1f, 1f, 1f);
                    consumer.addVertex(matrixStack.pose(), v.toVector3f()).setNormal(0, 1, 0).setColor(1f, 1f, 1f, 1f);
                    matrixStack.matrixPop();
                }
            });
        }
    }

    @Override
    public void renderHand(InteractionHand hand, MatrixStack poseStack, MultiBufferSource multiBufferSource, int packedLight, float partialTick, float interpolatedPitch, float swingProgress, float equipProgress, ItemStack stack) {

    }

    public void renderHud(GuiGraphics graphics, float partialTicks) {
        int centerX = graphics.guiWidth() / 2;
        float hookCooldownOffset = Math.clamp((hookCooldown) + (hookLocation == null ? partialTicks  : -partialTicks * 2), 0, 100) / 100.0f;

        graphics.fill(centerX - 125, graphics.guiHeight() - 50, (int) (centerX + 125 - ((1 - hookCooldownOffset) * 250)), graphics.guiHeight() - 45, 0x7FFFFFFF);
    }

    public static void updateGrapplingPlayers(GrappleUpdatePacket packet, IPayloadContext ctx) {
        GRAPPLING_PLAYERS.put(packet.playerUUID(), new Vec3(packet.x(), packet.y(), packet.z()));
    }

    public static void disperseGrapplingPlayerPacket(GrappleUpdatePacket packet, IPayloadContext ctx) {
        PacketDistributor.sendToAllPlayers(packet);
    }
}
