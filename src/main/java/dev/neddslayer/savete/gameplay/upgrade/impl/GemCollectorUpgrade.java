package dev.neddslayer.savete.gameplay.upgrade.impl;

import dev.neddslayer.savete.block.entity.GemstoneBlockEntity;
import dev.neddslayer.savete.gameplay.GameController;
import dev.neddslayer.savete.gameplay.upgrade.UpgradeRegistry;
import dev.neddslayer.savete.gameplay.upgrade.api.AbstractUpgrade;
import dev.neddslayer.savete.registrar.BlockRegistrar;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class GemCollectorUpgrade extends AbstractUpgrade {
    @Override
    public void applyToPlayer(Player player) {

    }

    @Override
    public void removeFromPlayer(Player player) {

    }

    @Override
    public float onGemstoneCollect(ServerLevel level, Vec3 pos, float count) {
        if (level.getRandom().nextDouble() < 0.1) {
            final boolean[] found = {false};
            BlockPos.betweenClosedStream(AABB.ofSize(pos, 40, 40, 40)).forEach(blockPos -> {
                if (found[0]) return;
                BlockState state = level.getBlockState(blockPos);
                if (state.is(BlockRegistrar.GEMSTONE)) {
                    ((GemstoneBlockEntity) level.getBlockEntity(blockPos)).markForDestroy();
                    // TODO: send vfx to client
                    found[0] = true;
                }
            });
        }

        return super.onGemstoneCollect(level, pos, count);
    }
}
