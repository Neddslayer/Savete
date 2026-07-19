package dev.neddslayer.savete.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.block.entity.NextLevelInfoBlockEntity;
import dev.neddslayer.savete.gameplay.LocalPlayerManager;
import dev.neddslayer.savete.render.GuiHelper;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;

public class NextLevelInfoRenderer implements BlockEntityRenderer<NextLevelInfoBlockEntity> {
    private final Font font;

    public NextLevelInfoRenderer(BlockEntityRendererProvider.Context ctx) {
        this.font = ctx.getFont();
    }

    @Override
    public void render(NextLevelInfoBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(90));

        PoseStack.Pose pose = poseStack.last();

        poseStack.translate(-1, -5, 0.01f);

        VertexConsumer consumer = multiBufferSource.getBuffer(RenderType.ENTITY_CUTOUT.apply(Savete.path("textures/next_level_info_screen.png")));
        consumer.addVertex(pose, 0, 0, 0).setColor(1f, 1f, 1f, 1f).setUv(0, 0).setNormal(pose, 1, 0, 0).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY);
        consumer.addVertex(pose, 6, 0, 0).setColor(1f, 1f, 1f, 1f).setUv(1, 0).setNormal(pose, 1, 0, 0).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY);
        consumer.addVertex(pose, 6, 6, 0).setColor(1f, 1f, 1f, 1f).setUv(1, 1).setNormal(pose, 1, 0, 0).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY);
        consumer.addVertex(pose, 0, 6, 0).setColor(1f, 1f, 1f, 1f).setUv(0, 1).setNormal(pose, 1, 0, 0).setLight(LightTexture.FULL_BRIGHT).setOverlay(OverlayTexture.NO_OVERLAY);

        poseStack.translate(0.4, 5.6, 0);
        poseStack.scale(0.03125F, -0.03125F, 0.03125F);

        GuiHelper.renderWobblyText(multiBufferSource, poseStack, this.font, Component.literal("Incoming Enemies").withStyle(ChatFormatting.RED), 96 - 12.8f - this.font.width("Incoming Enemies") / 2f, 0, 0xFFFFFFFF, partialTick, Minecraft.getInstance().levelRenderer.getTicks(), 1, 0.2, 0.1);

        float currentY = 20;
        Object2IntMap<EntityType<?>> enemyCounts = new Object2IntArrayMap<>();
        for (EntityType<?> entityType : LocalPlayerManager.predictedEnemies) {
            enemyCounts.computeInt(entityType, (t, i) -> {if (i == null) return 1; return i + 1;});
        }
        for (EntityType<?> entityType : enemyCounts.keySet()) {
            int count = enemyCounts.getInt(entityType);
            this.font.drawInBatch(Component.translatable(entityType.getDescriptionId() + ".name").append(Component.literal(" x" + count).withStyle(ChatFormatting.GRAY)), 0, currentY, 0xFFFFFFFF, false, poseStack.last().pose(), multiBufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, LightTexture.FULL_BRIGHT);
            poseStack.pushPose();
            poseStack.scale(0.5f, 0.5f, 0.5f);
            poseStack.translate(0, currentY + this.font.lineHeight, 0);
            this.font.drawInBatch(Component.translatable(entityType.getDescriptionId() + ".description").withStyle(ChatFormatting.DARK_GRAY), 0, currentY + this.font.lineHeight, 0xFFFFFFFF, false, poseStack.last().pose(), multiBufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
            currentY += this.font.lineHeight * 2.5f;
        }

        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(NextLevelInfoBlockEntity blockEntity) {
        return AABB.INFINITE;
    }
}
