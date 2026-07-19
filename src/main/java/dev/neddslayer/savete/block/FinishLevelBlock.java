package dev.neddslayer.savete.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.neddslayer.savete.block.entity.FinishLevelBlockEntity;
import dev.neddslayer.savete.gameplay.GameController;
import dev.neddslayer.savete.registrar.BlockEntityRegistrar;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class FinishLevelBlock extends BaseEntityBlock {
    private static final MapCodec<FinishLevelBlock> CODEC = simpleCodec(FinishLevelBlock::new);

    public FinishLevelBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, BlockEntityRegistrar.FINISH_LEVEL.get(), ((level1, blockPos, blockState, be) -> be.tick(level1, blockPos, blockState)));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new FinishLevelBlockEntity(blockPos, blockState);
    }
}
