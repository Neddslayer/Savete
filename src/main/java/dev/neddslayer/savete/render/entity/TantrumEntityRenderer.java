package dev.neddslayer.savete.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.entity.hostile.SquelchEntity;
import dev.neddslayer.savete.entity.hostile.TantrumEntity;
import dev.neddslayer.savete.render.entity.model.SquelchModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class TantrumEntityRenderer extends LateEntityRenderer<TantrumEntity> {

    protected final EntityModel<SquelchEntity> model;
    private static final ResourceLocation SQUELCH_LOCATION = Savete.path("textures/entity/squelch.png");

    public TantrumEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SquelchModel(context.bakeLayer(SquelchModel.LAYER_LOCATION));
    }

    @Override
    public void renderLate(TantrumEntity p_entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        if (p_entity.attackTimer < 10) {
            Vector3f dir = p_entity.getEntityData().get(TantrumEntity.TARGET_DIRECTION);
            Vector3f vOff = new Vector3f(0, 1, 0).cross(dir).normalize().mul(0.075f);

            PoseStack.Pose pose = poseStack.last();

            VertexConsumer indicatorConsumer = bufferSource.getBuffer(RenderType.DEBUG_QUADS);
            indicatorConsumer.addVertex(pose, vOff)                       .setColor(1f, 0, 0, 1.0f - (p_entity.attackTimer + partialTick) / 10f);
            indicatorConsumer.addVertex(pose, vOff.negate(new Vector3f())).setColor(1f, 0, 0, 1.0f - (p_entity.attackTimer + partialTick) / 10f);
            indicatorConsumer.addVertex(pose, dir.mul(10, new Vector3f()).add(vOff.negate(new Vector3f()))).setColor(1f, 0, 0, 1.0f - (p_entity.attackTimer + partialTick) / 10f);
            indicatorConsumer.addVertex(pose, dir.mul(10, new Vector3f()).add(vOff)).setColor(1f, 0, 0, 1.0f - (p_entity.attackTimer + partialTick) / 10f);
        }

        RenderType rendertype = this.model.renderType(SQUELCH_LOCATION);
        VertexConsumer vertexconsumer = bufferSource.getBuffer(rendertype);
        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 654311423);

        poseStack.popPose();
        super.render(p_entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public boolean shouldRender(TantrumEntity livingEntity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(TantrumEntity squelchEntity) {
        return SQUELCH_LOCATION;
    }
}
