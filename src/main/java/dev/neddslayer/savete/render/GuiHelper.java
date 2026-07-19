package dev.neddslayer.savete.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class GuiHelper {
    private GuiHelper() {}

    public static void drawWobblyText(GuiGraphics graphics, Font font, Component text, int x, int y, int color, float partialTick, int ticks, double wobble, double length, double speed) {
        AtomicInteger currentX = new AtomicInteger(x);
        text.visit((style, string) -> {
            for (int i = 0; i < string.length(); i++) {
                graphics.pose().pushPose();
                graphics.pose().translate(0, y + wobble * Math.sin(speed * (ticks + partialTick) + currentX.get() * length), 0);
                graphics.drawString(font, Component.literal(String.valueOf(string.charAt(i))).withStyle(style), currentX.get(), 0, color);
                currentX.addAndGet(font.width(String.valueOf(string.charAt(i))));
                graphics.pose().popPose();
            }
            return Optional.empty();
        }, Style.EMPTY);
    }

    public static void renderWobblyText(MultiBufferSource graphics, PoseStack poseStack, Font font, Component text, float x, float y, int color, float partialTick, int ticks, double wobble, double length, double speed) {
        AtomicReference<Float> currentX = new AtomicReference<>(x);
        text.visit((style, string) -> {
            for (int i = 0; i < string.length(); i++) {
                poseStack.pushPose();
                poseStack.translate(0, y + wobble * Math.sin(speed * (ticks + partialTick) + currentX.get() * length), 0);
                font.drawInBatch(Component.literal(String.valueOf(string.charAt(i))).withStyle(style), currentX.get(), 0, color, false, poseStack.last().pose(), graphics, Font.DisplayMode.POLYGON_OFFSET, 0, LightTexture.FULL_BRIGHT);
                int finalI = i;
                currentX.updateAndGet(v -> v + (font.width(String.valueOf(string.charAt(finalI)))));
                poseStack.popPose();
            }
            return Optional.empty();
        }, Style.EMPTY);
    }

    public static String upgradeLocationToTranslatable(ResourceLocation location) {
        return "upgrade." + location.getNamespace() + "." + location.getPath();
    }
}
