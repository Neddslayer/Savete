package dev.neddslayer.savete.render.entity;

import com.mojang.blaze3d.vertex.*;
import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.entity.hostile.HaloEntity;
import dev.neddslayer.savete.entity.hostile.SquelchEntity;
import dev.neddslayer.savete.render.entity.model.SquelchModel;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.client.util.Easing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class HaloEntityRenderer extends LateEntityRenderer<HaloEntity> {

    protected final EntityModel<SquelchEntity> model;
    private static final ResourceLocation SQUELCH_LOCATION = Savete.path("textures/entity/squelch.png");

    public HaloEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SquelchModel(context.bakeLayer(SquelchModel.LAYER_LOCATION));
    }

    @Override
    public void renderLate(HaloEntity p_entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        VertexConsumer builder = bufferSource.getBuffer(VeilRenderType.get(Savete.path("halo_fan")));

        float scale = Easing.EASE_OUT_CUBIC.ease(Math.clamp((p_entity.attackTimer - 40 + partialTick) / 10f, 0, 1));
        poseStack.scale(1.5f + scale * 2.5f, 1.5f + scale * 2.5f, 1.5f + scale * 2.5f);

        float colorScale = Math.clamp((p_entity.attackTimer - 40 + partialTick) / 30f, 0, 1);

        builder.addVertex(poseStack.last(), 0,0,0).setColor(1f, colorScale, colorScale, 1f).setNormal(0, 1, 0);
        for (int i = 0; i <= 64; i++) {
            float percent = i / 64f;
            builder.addVertex(poseStack.last(), Mth.cos(percent * Mth.TWO_PI), 0.001f, Mth.sin(percent * Mth.TWO_PI)).setColor(1f, colorScale, colorScale, colorScale).setNormal(0, 1, 0);
        }

        if (p_entity.attackTimer > 70) {
            VertexConsumer builder2 = bufferSource.getBuffer(RenderType.DEBUG_QUADS);

            for (int i = 1; i <= 64; i++) {
                float prevPercent = (i - 1) / 64f;
                float percent = i / 64f;
                builder2.addVertex(poseStack.last(), Mth.cos(prevPercent * Mth.TWO_PI), 0.001f, Mth.sin(prevPercent * Mth.TWO_PI)).setColor(1f, 1f, 1f, 1f).setNormal(0, 1, 0);
                builder2.addVertex(poseStack.last(), Mth.cos(prevPercent * Mth.TWO_PI), 2f, Mth.sin(prevPercent * Mth.TWO_PI)).setColor(1f, 1f, 1f, 1f).setNormal(0, 1, 0);
                builder2.addVertex(poseStack.last(), Mth.cos(percent * Mth.TWO_PI), 2f, Mth.sin(percent * Mth.TWO_PI)).setColor(1f, 1f, 1f, 1f).setNormal(0, 1, 0);
                builder2.addVertex(poseStack.last(), Mth.cos(percent * Mth.TWO_PI), 0.001f, Mth.sin(percent * Mth.TWO_PI)).setColor(1f, 1f, 1f, 1f).setNormal(0, 1, 0);
            }
        }

        poseStack.popPose();
        super.render(p_entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public boolean shouldRender(HaloEntity livingEntity, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(HaloEntity squelchEntity) {
        return SQUELCH_LOCATION;
    }
}
