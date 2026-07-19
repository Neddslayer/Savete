package dev.neddslayer.savete.registrar;

import dev.neddslayer.savete.block.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import static dev.neddslayer.savete.Savete.MODID;

public class BlockRegistrar {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    public static final DeferredBlock<VoidTorchBlock> VOID_TORCH = BLOCKS.register("void_torch", () -> new VoidTorchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));
    public static final DeferredBlock<GemstoneBlock> GEMSTONE = BLOCKS.register("gemstone", () -> new GemstoneBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE).instabreak()));
    public static final DeferredBlock<LauncherBlock> LAUNCHER = BLOCKS.register("launcher", () -> new LauncherBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK)));
    public static final DeferredBlock<FinishLevelBlock> FINISH_LEVEL = BLOCKS.register("finish_level", () -> new FinishLevelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK).noCollission()));
    public static final DeferredBlock<NextLevelInfoBlock> NEXT_LEVEL_INFO = BLOCKS.register("next_level_info", () -> new NextLevelInfoBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK).noCollission()));

    public static void bootstrap(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
