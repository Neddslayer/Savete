package dev.neddslayer.savete.block.entity;

import dev.neddslayer.savete.registrar.BlockEntityRegistrar;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class NextLevelInfoBlockEntity extends BlockEntity {
    public NextLevelInfoBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistrar.NEXT_LEVEL_INFO.get(), pos, blockState);
    }
}
