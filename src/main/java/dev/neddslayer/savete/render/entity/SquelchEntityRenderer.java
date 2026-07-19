package dev.neddslayer.savete.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.entity.hostile.SquelchEntity;
import dev.neddslayer.savete.render.entity.model.SquelchModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SquelchEntityRenderer extends EntityRenderer<SquelchEntity> {

    protected final EntityModel<SquelchEntity> model;
    private static final ResourceLocation SQUELCH_LOCATION = Savete.path("textures/entity/squelch.png");

    public SquelchEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SquelchModel(context.bakeLayer(SquelchModel.LAYER_LOCATION));
    }

    @Override
    public void render(SquelchEntity p_entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        float value = 1.0f - ((p_entity.jumpingTime + partialTick) / 20);
        if (value < 0) {
            value = (Mth.sin((p_entity.jumpingTime + partialTick) * Mth.PI * 0.1f) * -0.5f) / ((p_entity.jumpingTime + partialTick - 20) * 0.5f);
        }

        poseStack.scale(1 - value / 2, 1 + value, 1 - value / 2);

        poseStack.scale(2, 2, 2);

        RenderType rendertype = this.model.renderType(SQUELCH_LOCATION);
        VertexConsumer vertexconsumer = bufferSource.getBuffer(rendertype);
        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 654311423);

        poseStack.popPose();
        super.render(p_entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SquelchEntity squelchEntity) {
        return SQUELCH_LOCATION;
    }
}
