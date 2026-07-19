package dev.neddslayer.savete.gameplay.upgrade.api;

import net.minecraft.client.gui.GuiGraphics;

public interface IClientUpgrade {
    void renderHud(GuiGraphics graphics, float partialTick);
    void clientTick();
    default void onHitGrapplePoint() {

    }
}
