package dev.neddslayer.savete.gameplay.tile;

import dev.neddslayer.savete.Savete;
import foundry.veil.api.resource.VeilDynamicRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class TileRegistry {
    private static RegistryAccess registryAccess = RegistryAccess.EMPTY;

    public static final ResourceKey<Registry<TileData>> TILE_DATA = ResourceKey.createRegistryKey(Savete.path("tile_data"));
    private static final RegistryDataLoader.RegistryData<?> REGISTRY = new RegistryDataLoader.RegistryData<>(TILE_DATA, TileData.DIRECT_CODEC, true);

    public static RegistryAccess registryAccess() {
        return registryAccess;
    }

    @ApiStatus.Internal
    public static class Reloader implements PreparableReloadListener {
        @Override
        public @NotNull CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparationBarrier, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller preparationsProfiler, @NotNull ProfilerFiller reloadProfiler, @NotNull Executor backgroundExecutor, @NotNull Executor gameExecutor) {
            return VeilDynamicRegistry.loadRegistries(resourceManager, List.of(REGISTRY), backgroundExecutor)
                    .thenCompose(preparationBarrier::wait)
                    .thenAcceptAsync(data -> {
                        registryAccess = data.registryAccess();

                        String msg = VeilDynamicRegistry.printErrors(data.errors());
                        if (msg != null) {
                            Savete.LOGGER.error("Error loading tile data: {}\n", msg);
                        }

                        Savete.LOGGER.info("Loaded {} tiles", registryAccess.registryOrThrow(TILE_DATA).size());
                    }, gameExecutor);
        }

        @Override
        public @NotNull String getName() {
            return TileRegistry.class.getSimpleName();
        }
    }
}
