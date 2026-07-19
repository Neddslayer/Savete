package dev.neddslayer.savete.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.neddslayer.savete.SaveteClient;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class LateBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    protected LateBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T t, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
        SaveteClient.INSTANCE.BLOCK_ENTITY_QUEUE.add(t);
    }

    public abstract void renderLate(BlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay);
}
