package dev.neddslayer.savete.block;

import com.mojang.serialization.MapCodec;
import dev.neddslayer.savete.block.entity.NextLevelInfoBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class NextLevelInfoBlock extends BaseEntityBlock {
    private static final MapCodec<NextLevelInfoBlock> CODEC = simpleCodec(NextLevelInfoBlock::new);

    public NextLevelInfoBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new NextLevelInfoBlockEntity(blockPos, blockState);
    }
}
