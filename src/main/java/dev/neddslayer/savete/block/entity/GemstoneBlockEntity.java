package dev.neddslayer.savete.block.entity;

import dev.neddslayer.savete.gameplay.GameController;
import dev.neddslayer.savete.registrar.BlockEntityRegistrar;
import dev.neddslayer.savete.registrar.BlockRegistrar;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GemstoneBlockEntity extends BlockEntity {
    private int breakDelay = -1;

    public GemstoneBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistrar.GEMSTONE.get(), pos, blockState);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide) {
            if (breakDelay == 0) {
                markOthersForDestroy(level, pos);
                level.destroyBlock(pos, false);
                if (level instanceof ServerLevel server) {
                    GameController.INSTANCE.crackGemstone(pos.getCenter(), server);
                }
            }
            if (breakDelay > 0) breakDelay--;
        }
    }

    private static void tryMarkForDestroy(Level level, BlockPos pos) {
        if (level.getBlockState(pos).is(BlockRegistrar.GEMSTONE)) {
            GemstoneBlockEntity e = (GemstoneBlockEntity) level.getBlockEntity(pos);
            if (e != null) e.markForDestroy();
        }
    }

    public void markForDestroy() {
        breakDelay = 1;
    }

    public static void markOthersForDestroy(Level level, BlockPos pos) {
        tryMarkForDestroy(level, pos.above());
        tryMarkForDestroy(level, pos.below());
        tryMarkForDestroy(level, pos.north());
        tryMarkForDestroy(level, pos.south());
        tryMarkForDestroy(level, pos.east());
        tryMarkForDestroy(level, pos.west());
    }
}
