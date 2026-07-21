package dev.neddslayer.savete;

import dev.neddslayer.savete.datagen.SaveteBlockStates;
import dev.neddslayer.savete.datagen.SaveteLangProvider;
import dev.neddslayer.savete.entity.UpgradeHolderEntity;
import dev.neddslayer.savete.entity.hostile.SquelchEntity;
import dev.neddslayer.savete.gameplay.GameController;
import dev.neddslayer.savete.gameplay.LocalPlayerManager;
import dev.neddslayer.savete.gameplay.movement.GrappleController;
import dev.neddslayer.savete.gameplay.tile.TileRegistry;
import dev.neddslayer.savete.gameplay.upgrade.api.IClientUpgrade;
import dev.neddslayer.savete.gameplay.upgrade.UpgradeRegistry;
import dev.neddslayer.savete.network.*;
import dev.neddslayer.savete.registrar.BlockEntityRegistrar;
import dev.neddslayer.savete.registrar.BlockRegistrar;
import dev.neddslayer.savete.registrar.EntityRegistrar;
import dev.neddslayer.savete.registrar.ItemRegistrar;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.quasar.particle.ParticleEmitter;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.MainThreadPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Savete.MODID)
public class Savete {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "savete";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LoggerFactory.getLogger("Savete");

    public Savete(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        ItemRegistrar.bootstrap(modEventBus);
        BlockRegistrar.bootstrap(modEventBus);
        BlockEntityRegistrar.bootstrap(modEventBus);
        EntityRegistrar.bootstrap(modEventBus);
        UpgradeRegistry.bootstrap(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (VoidTunnels) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::onGatherData);
        modEventBus.addListener(this::onRegisterPackets);
        modEventBus.addListener(this::onRegister);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    private void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Register the provider.
        generator.addProvider(
                event.includeClient() || event.includeServer(),
                new SaveteBlockStates(output, existingFileHelper)
        );
        generator.addProvider(
                event.includeClient(),
                new SaveteLangProvider(output, MODID, "en_us")
        );
    }

    private void onRegisterPackets(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(
                GrappleUpdatePacket.TYPE,
                GrappleUpdatePacket.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        GrappleController::updateGrapplingPlayers, // client
                        GrappleController::disperseGrapplingPlayerPacket // server
                )
        );
        registrar.playToClient(
                UpdateGemstoneCountPacket.TYPE,
                UpdateGemstoneCountPacket.STREAM_CODEC,
                new MainThreadPayloadHandler<>((packet, ctx) -> {
                    LocalPlayerManager.gemstoneCount = packet.newTotal();
                    LocalPlayerManager.rawGemsBroken = packet.rawCount();
                    LocalPlayerManager.totalRawGems = packet.totalGemstonesInLevel();
                    if (!Float.isNaN(packet.position().x)) {
                        ParticleEmitter emitter = VeilRenderSystem.renderer().getParticleManager().createEmitter(Savete.path("gemstone_collect"));
                        emitter.setPosition(packet.position().get(new Vector3d()));
                        VeilRenderSystem.renderer().getParticleManager().addParticleSystem(emitter);
                    }
                })
        );
        registrar.playToClient(
                AddClientUpgrade.TYPE,
                AddClientUpgrade.STREAM_CODEC,
                (addClientUpgrade, iPayloadContext) ->
                        LocalPlayerManager.clientUpgrades.add((IClientUpgrade) addClientUpgrade.upgradeType().getUpgrade())
        );
        registrar.playToClient(
                ResetClientUpgradesPacket.TYPE,
                ResetClientUpgradesPacket.STREAM_CODEC,
                (resetClientUpgradesPacket, iPayloadContext) ->
                        LocalPlayerManager.clientUpgrades.clear()
        );
        registrar.playToClient(
                SetUpgradesPacket.TYPE,
                SetUpgradesPacket.STREAM_CODEC,
                (setUpgradesPacket, iPayloadContext) -> {
                    if (setUpgradesPacket.append()) {
                        LocalPlayerManager.currentUpgrades.addAll(setUpgradesPacket.upgrades());
                    } else {
                        LocalPlayerManager.currentUpgrades = new ArrayList<>(setUpgradesPacket.upgrades());
                    }
                }
        );
        registrar.playToClient(
                UpdateLevelInfoPacket.TYPE,
                UpdateLevelInfoPacket.STREAM_CODEC,
                (updateLevelInfoPacket, iPayloadContext) -> {
                    LocalPlayerManager.currentLevel = updateLevelInfoPacket.level();
                    LocalPlayerManager.inIntermission = updateLevelInfoPacket.intermission();
                }
        );
        registrar.playToClient(
                FinishLevelPacket.TYPE,
                FinishLevelPacket.STREAM_CODEC,
                (finishLevelPacket, iPayloadContext) -> {
                    SaveteClient.forceFullOverlay = finishLevelPacket.overlay();
                }
        );
        registrar.playToClient(
                SpawnQuasarParticlePacket.TYPE,
                SpawnQuasarParticlePacket.STREAM_CODEC,
                (spawnQuasarParticlePacket, iPayloadContext) -> {
                    try {
                        ParticleEmitter emitter = VeilRenderSystem.renderer().getParticleManager().createEmitter(spawnQuasarParticlePacket.location());
                        emitter.setPosition(spawnQuasarParticlePacket.position().get(new Vector3d()));
                        VeilRenderSystem.renderer().getParticleManager().addParticleSystem(emitter);
                    } catch (Exception e) {
                        Savete.LOGGER.warn("Failed to spawn packet {} at {}", spawnQuasarParticlePacket.location(), spawnQuasarParticlePacket.position());
                    }
                }
        );
        registrar.playToClient(
                SetPredictedEnemiesPacket.TYPE,
                SetPredictedEnemiesPacket.STREAM_CODEC,
                (setPredictedEnemiesPacket, iPayloadContext) -> {
                    LocalPlayerManager.predictedEnemies = setPredictedEnemiesPacket.entities();
                }
        );
        registrar.playToServer(
                ForceServerPlayerDeltaMovement.TYPE,
                ForceServerPlayerDeltaMovement.STREAM_CODEC,
                (forceServerPlayerDeltaMovement, iPayloadContext) -> {
                    iPayloadContext.player().setDeltaMovement(forceServerPlayerDeltaMovement.delta().x, forceServerPlayerDeltaMovement.delta().y, forceServerPlayerDeltaMovement.delta().z);
                }
        );
    }

    private void onRegister(RegisterEvent event) {
        event.register(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, Savete.path("upgrade_type"), UpgradeHolderEntity.UPGRADE_LOCATION::serializer);
        event.register(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, Savete.path("jump_path"), SquelchEntity.JUMP_PATH::serializer);
        if (event.getRegistryKey().equals(Registries.STRUCTURE_PROCESSOR)) {
            GameController.SaveteTileProcessor.TYPE = Registry.<StructureProcessorType<?>>register((Registry<? super StructureProcessorType<?>>) event.getRegistry(), "tile_processor", (StructureProcessorType<GameController.SaveteTileProcessor>) (() -> GameController.SaveteTileProcessor.CODEC));
        }
    }

    @SubscribeEvent
    private void onRegisterListeners(AddReloadListenerEvent event) {
        event.addListener(new TileRegistry.Reloader());
    }

    @SubscribeEvent
    private void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("set_gem").executes(ctx -> {
            GameController.INSTANCE.gemstones += 1000;
            PacketDistributor.sendToPlayersInDimension(ctx.getSource().getLevel(), new UpdateGemstoneCountPacket(GameController.INSTANCE.gemstones, 0, 0, new Vector3f(Float.NaN)));
            return 1;
        }));
    }

    public static ResourceLocation path(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
