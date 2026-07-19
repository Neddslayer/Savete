package dev.neddslayer.savete.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.block.entity.FinishLevelBlockEntity;
import dev.neddslayer.savete.entity.hostile.HeliosEntity;
import dev.neddslayer.savete.entity.hostile.SquelchEntity;
import dev.neddslayer.savete.render.entity.model.SquelchModel;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.util.Easing;
import foundry.veil.api.flare.EffectHost;
import foundry.veil.api.flare.FlareEffectManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class HeliosEntityRenderer extends LateEntityRenderer<HeliosEntity> {

    protected final EntityModel<SquelchEntity> model;
    private static final ResourceLocation SQUELCH_LOCATION = Savete.path("textures/entity/squelch.png");

    public HeliosEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SquelchModel(context.bakeLayer(SquelchModel.LAYER_LOCATION));
    }

    @Override
    public void renderLate(HeliosEntity p_entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        Vector3f start = p_entity.getEntityData().get(HeliosEntity.TARGET_POSITION);
        Vec3 pos = p_entity.getPosition(partialTick);

        poseStack.translate(-pos.x, -pos.y, -pos.z);

        Vector3f dir = p_entity.getEntityData().get(HeliosEntity.TARGET_BEAM_POSITION).sub(start, new Vector3f()).normalize().mul(30);
        Vector3f end = start.add(dir, new Vector3f());
        Vector3f normal = end.sub(start, new Vector3f()).normalize();
        Vector3f vOff = new Vector3f(0, 1, 0).cross(normal).normalize().mul(0.075f);

        PoseStack.Pose pose = poseStack.last();

        if (p_entity.attackTimer > 10 && p_entity.attackTimer <= 30 && p_entity.getEntityData().get(HeliosEntity.TARGET_BEAM_POSITION).length() > 0.1f) {
            EffectHost host = new EffectHost() {
                @Override
                public float getValue(String name) {
                    return 0;
                }

                @Override
                public String getName() {
                    return "savete:helios_beam";
                }

                @Override
                public void update(float partialTick) {

                }
            };
            poseStack.translate(pos.x, pos.y, pos.z);
            Vector3f rotateAxis = new Vector3f(0, 1, 0).cross(normal).normalize();

            poseStack.mulPose(new Quaternionf().rotateAxis(Math.clamp((p_entity.attackTimer - 11 + partialTick) / 20f, 0, 1) * Mth.PI - Mth.HALF_PI, rotateAxis).rotateY((float) Math.atan2(dir.x, dir.z)));
            FlareEffectManager.getTemplate(Savete.path("helios_beam")).render(host, VeilRenderBridge.create(poseStack), partialTick);
        } else if (p_entity.getEntityData().get(HeliosEntity.TARGET_BEAM_POSITION).length() > 0.1f) {
            VertexConsumer warnConsumer = bufferSource.getBuffer(RenderType.DEBUG_QUADS);

            float a = Math.clamp(1.0f - ((p_entity.attackTimer + partialTick) / 10f), 0, 1) * 0.5f;
            warnConsumer.addVertex(pose, start.add(vOff,                        new Vector3f()).add(0, 15, 0)).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT).setColor(1f, 0f, 0f, a).setNormal(pose, normal.x, normal.y, normal.z);
            warnConsumer.addVertex(pose, start.add(vOff.negate(new Vector3f()), new Vector3f()).add(0, 15, 0)).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT).setColor(1f, 0f, 0f, a).setNormal(pose, normal.x, normal.y, normal.z);
            warnConsumer.addVertex(pose, end  .add(vOff.negate(new Vector3f()), new Vector3f()).add(0, 15, 0)).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT).setColor(1f, 0f, 0f, a).setNormal(pose, normal.x, normal.y, normal.z);
            warnConsumer.addVertex(pose, end  .add(vOff,                        new Vector3f()).add(0, 15, 0)).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT).setColor(1f, 0f, 0f, a).setNormal(pose, normal.x, normal.y, normal.z);

            warnConsumer.addVertex(pose, start.add(vOff,                        new Vector3f()).sub(0, 15, 0)).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT).setColor(1f, 0f, 0f, a).setNormal(pose, normal.x, normal.y, normal.z);
            warnConsumer.addVertex(pose, start.add(vOff.negate(new Vector3f()), new Vector3f()).sub(0, 15, 0)).setUv(1, 0).setLight(LightTexture.FULL_BRIGHT).setColor(1f, 0f, 0f, a).setNormal(pose, normal.x, normal.y, normal.z);
            warnConsumer.addVertex(pose, end  .add(vOff.negate(new Vector3f()), new Vector3f()).sub(0, 15, 0)).setUv(1, 1).setLight(LightTexture.FULL_BRIGHT).setColor(1f, 0f, 0f, a).setNormal(pose, normal.x, normal.y, normal.z);
            warnConsumer.addVertex(pose, end  .add(vOff,                        new Vector3f()).sub(0, 15, 0)).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT).setColor(1f, 0f, 0f, a).setNormal(pose, normal.x, normal.y, normal.z);

            warnConsumer.addVertex(pose, start.add(vOff,                        new Vector3f()).add(0, 15, 0)).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT).setColor(1f, 0f, 0f, a).setNormal(pose, normal.x, normal.y, normal.z);
            warnConsumer.addVertex(pose, end  .add(vOff,                        new Vector3f()).add(0, 15, 0)).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT).setColor(1f, 0f, 0f, a).setNormal(pose, normal.x, normal.y, normal.z);
            warnConsumer.addVertex(pose, end  .add(vOff,                        new Vector3f()).sub(0, 15, 0)).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT).setColor(1f, 0f, 0f, a).setNormal(pose, normal.x, normal.y, normal.z);
            warnConsumer.addVertex(pose, start.add(vOff,                        new Vector3f()).sub(0, 15, 0)).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT).setColor(1f, 0f, 0f, a).setNormal(pose, normal.x, normal.y, normal.z);

            warnConsumer.addVertex(pose, start.add(vOff.negate(new Vector3f()), new Vector3f()).add(0, 15, 0)).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT).setColor(1f, 0f, 0f, a).setNormal(pose, normal.x, normal.y, normal.z);
            warnConsumer.addVertex(pose, end  .add(vOff.negate(new Vector3f()), new Vector3f()).add(0, 15, 0)).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT).setColor(1f, 0f, 0f, a).setNormal(pose, normal.x, normal.y, normal.z);
            warnConsumer.addVertex(pose, end  .add(vOff.negate(new Vector3f()), new Vector3f()).sub(0, 15, 0)).setUv(0, 1).setLight(LightTexture.FULL_BRIGHT).setColor(1f, 0f, 0f, a).setNormal(pose, normal.x, normal.y, normal.z);
            warnConsumer.addVertex(pose, start.add(vOff.negate(new Vector3f()), new Vector3f()).sub(0, 15, 0)).setUv(0, 0).setLight(LightTexture.FULL_BRIGHT).setColor(1f, 0f, 0f, a).setNormal(pose, normal.x, normal.y, normal.z);
        }

        poseStack.popPose();

        poseStack.pushPose();

        RenderType rendertype = this.model.renderType(SQUELCH_LOCATION);
        VertexConsumer vertexconsumer = bufferSource.getBuffer(rendertype);
        this.model.renderToBuffer(poseStack, vertexconsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 654311423);

        poseStack.popPose();
        super.render(p_entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public boolean shouldRender(HeliosEntity livingEntity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(HeliosEntity squelchEntity) {
        return SQUELCH_LOCATION;
    }
}
