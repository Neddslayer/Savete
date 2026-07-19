package dev.neddslayer.savete.registrar;

import dev.neddslayer.savete.block.entity.FinishLevelBlockEntity;
import dev.neddslayer.savete.block.entity.GemstoneBlockEntity;
import dev.neddslayer.savete.block.entity.NextLevelInfoBlockEntity;
import dev.neddslayer.savete.block.entity.VoidTorchBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static dev.neddslayer.savete.Savete.MODID;

public class BlockEntityRegistrar {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VoidTorchBlockEntity>> VOID_TORCH = BLOCK_ENTITIES.register("void_torch", () -> BlockEntityType.Builder.of(VoidTorchBlockEntity::new, BlockRegistrar.VOID_TORCH.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GemstoneBlockEntity>> GEMSTONE = BLOCK_ENTITIES.register("gemstone", () -> BlockEntityType.Builder.of(GemstoneBlockEntity::new, BlockRegistrar.GEMSTONE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FinishLevelBlockEntity>> FINISH_LEVEL = BLOCK_ENTITIES.register("finish_level", () -> BlockEntityType.Builder.of(FinishLevelBlockEntity::new, BlockRegistrar.FINISH_LEVEL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NextLevelInfoBlockEntity>> NEXT_LEVEL_INFO = BLOCK_ENTITIES.register("next_level_info", () -> BlockEntityType.Builder.of(NextLevelInfoBlockEntity::new, BlockRegistrar.NEXT_LEVEL_INFO.get()).build(null));

    public static void bootstrap(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
