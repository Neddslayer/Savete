package dev.neddslayer.savete.gameplay.movement;

import com.mojang.blaze3d.vertex.PoseStack;
import foundry.veil.api.client.render.MatrixStack;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.InputEvent;

public interface IPlayerController {

    void onClick(InputEvent.MouseButton event);

    void tick();

    void renderWorld(MatrixStack matrixStack, Camera camera, MultiBufferSource bufferSource, float partialTicks);

    void renderHand(InteractionHand hand, MatrixStack poseStack, MultiBufferSource multiBufferSource, int packedLight,
                    float partialTick, float interpolatedPitch,
                    float swingProgress, float equipProgress, ItemStack stack);

    void renderHud(GuiGraphics graphics, float partialTicks);

    void onDeEquip();

    void onEquip();
}
