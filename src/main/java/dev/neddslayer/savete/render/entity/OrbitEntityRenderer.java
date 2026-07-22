package dev.neddslayer.savete.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.entity.hostile.SquelchEntity;
import dev.neddslayer.savete.entity.hostile.OrbitEntity;
import dev.neddslayer.savete.render.entity.model.SquelchModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class OrbitEntityRenderer extends LateEntityRenderer<OrbitEntity> {

    protected final EntityModel<SquelchEntity> model;
    private static final ResourceLocation SQUELCH_LOCATION = Savete.path("textures/entity/squelch.png");

    public OrbitEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SquelchModel(context.bakeLayer(SquelchModel.LAYER_LOCATION));
    }

    @Override
    public void renderLate(OrbitEntity p_entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees((float) (p_entity.velocity.length() * (p_entity.age + partialTick))));

        RenderType rendertype = this.model.renderType(SQUELCH_LOCATION);
        VertexConsumer vertexconsumer = bufferSource.getBuffer(rendertype);
        this.model.renderToBuffer(poseStack, vertexconsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 654311423);

        poseStack.popPose();
        super.render(p_entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public boolean shouldRender(OrbitEntity livingEntity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(OrbitEntity squelchEntity) {
        return SQUELCH_LOCATION;
    }
}
