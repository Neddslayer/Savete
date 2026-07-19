package dev.neddslayer.savete.gameplay.upgrade.impl;

import dev.neddslayer.savete.gameplay.upgrade.api.AbstractUpgrade;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class GemMultiplierUpgrade extends AbstractUpgrade {
    @Override
    public void applyToPlayer(Player player) {

    }

    @Override
    public void removeFromPlayer(Player player) {

    }

    @Override
    public float onGemstoneCollect(ServerLevel level, Vec3 pos, float count) {
        return count * 1.5f;
    }
}
