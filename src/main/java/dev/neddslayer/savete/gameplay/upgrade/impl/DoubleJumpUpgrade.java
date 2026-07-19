package dev.neddslayer.savete.gameplay.upgrade.impl;

import dev.neddslayer.savete.gameplay.upgrade.api.AbstractUpgrade;
import dev.neddslayer.savete.gameplay.upgrade.api.IClientUpgrade;
import dev.neddslayer.savete.mixin.LivingEntityMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class DoubleJumpUpgrade extends AbstractUpgrade implements IClientUpgrade {
    private boolean doubleJumpAvailable;

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
        if (player.onGround()) {
            this.doubleJumpAvailable = true;
        }

        if (Minecraft.getInstance().options.keyJump.consumeClick() && !player.onGround() && this.doubleJumpAvailable) {
            Vec3 velocity = player.getDeltaMovement();
            player.setDeltaMovement(velocity.x * 1.1, ((LivingEntityMixin) player).savete$jumpPower(), velocity.z * 1.1);
            this.doubleJumpAvailable = false;
        }
    }

    @Override
    public void onHitGrapplePoint() {
        this.doubleJumpAvailable = true;
    }
}
