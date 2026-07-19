package dev.neddslayer.savete.gameplay.upgrade.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractUpgrade {
    public abstract void applyToPlayer(Player player);

    public abstract void removeFromPlayer(Player player);

    public void tick(ServerLevel level, ServerPlayer player) {
    }

    public float onGemstoneCollect(ServerLevel level, Vec3 pos, float count) {
        return count;
    }
}
