package dev.neddslayer.savete.gameplay.upgrade.impl;

import dev.neddslayer.savete.gameplay.upgrade.api.AbstractUpgrade;
import dev.neddslayer.savete.gameplay.upgrade.api.IClientUpgrade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class FastFallUpgrade extends AbstractUpgrade implements IClientUpgrade {
    @Override
    public void applyToPlayer(Player player) {
    }

    @Override
    public void removeFromPlayer(Player player) {
    }

    @Override
    public void renderHud(GuiGraphics graphics, float partialTick) {
    }

    @Override
    public void clientTick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!player.onGround() && player.isShiftKeyDown()) {
            Vec3 velocity = player.getDeltaMovement();
            player.setDeltaMovement(velocity.x, velocity.y - 0.04, velocity.z);
        }
    }
}
