package dev.neddslayer.savete.entity;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;

public interface IVoidTooltipHolder {

    void renderTooltip(GuiGraphics graphics, int x, int y);

    default Vec3 tooltipPositionOffset() {
        return Vec3.ZERO;
    }
}
