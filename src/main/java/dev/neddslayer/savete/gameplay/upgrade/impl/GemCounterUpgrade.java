package dev.neddslayer.savete.gameplay.upgrade.impl;

import dev.neddslayer.savete.gameplay.LocalPlayerManager;
import dev.neddslayer.savete.gameplay.upgrade.api.AbstractUpgrade;
import dev.neddslayer.savete.gameplay.upgrade.api.IClientUpgrade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class GemCounterUpgrade extends AbstractUpgrade implements IClientUpgrade {
    @Override
    public void applyToPlayer(Player player) {
    }

    @Override
    public void removeFromPlayer(Player player) {
    }

    @Override
    public void renderHud(GuiGraphics graphics, float partialTick) {
        if (LocalPlayerManager.totalRawGems > 0) {
            graphics.pose().pushPose();

            //graphics.pose().translate((float) graphics.guiWidth() / 2, graphics.guiHeight(), 0);
            graphics.pose().scale(1.25f, 1.25f, 1.25f);
            graphics.drawCenteredString(Minecraft.getInstance().font, Component.literal(LocalPlayerManager.rawGemsBroken + " / " + LocalPlayerManager.totalRawGems), (int) ((graphics.guiWidth() / 2.0) / 1.25), 25, 0xFFFF00FF);

            graphics.pose().popPose();
        }
    }

    @Override
    public void clientTick() {

    }
}
