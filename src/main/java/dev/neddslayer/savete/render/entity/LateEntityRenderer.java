package dev.neddslayer.savete.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neddslayer.savete.SaveteClient;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public abstract class LateEntityRenderer<T extends Entity> extends EntityRenderer<T> {
    protected LateEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(T p_entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        SaveteClient.INSTANCE.LATE_ENTITY_QUEUE.put(p_entity, this);
    }

    public abstract void renderLate(T p_entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight);

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return null;
    }
}
