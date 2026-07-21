package dev.neddslayer.savete.gameplay;

import com.mojang.serialization.MapCodec;
import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.entity.*;
import dev.neddslayer.savete.gameplay.tile.TileData;
import dev.neddslayer.savete.gameplay.tile.TileRegistry;
import dev.neddslayer.savete.gameplay.upgrade.api.AbstractUpgrade;
import dev.neddslayer.savete.gameplay.upgrade.api.IClientUpgrade;
import dev.neddslayer.savete.gameplay.upgrade.UpgradeRegistry;
import dev.neddslayer.savete.gameplay.upgrade.UpgradeType;
import dev.neddslayer.savete.network.*;
import dev.neddslayer.savete.registrar.BlockRegistrar;
import dev.neddslayer.savete.registrar.EntityRegistrar;
import dev.neddslayer.savete.registrar.ItemRegistrar;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;

import static net.minecraft.world.level.block.entity.StructureBlockEntity.createRandom;

@EventBusSubscriber
public class GameController {
    public static final GameController INSTANCE = new GameController();
    public static final ResourceKey<Level> VOID_TUNNELS_KEY = ResourceKey.create(Registries.DIMENSION, Savete.path("void_tunnels"));

    private static final RandomSource random = RandomSource.create();

    boolean gamePrepped = false;
    boolean gameActive = false;
    boolean intermissionActive = false;
    boolean intermissionPrepped = false;
    private int intermissionReadied = 0;
    private int intermissionDelay = -1;
    private long currentGameId = -1;
    private final Map<ChunkPos, Long> chunkInstanceMap = new HashMap<>();
    public float gemstones;
    private int rawGems, totalRawGems;
    public int currentLevel = 0;
    private int furthestZ = 0;
    private final List<Entity> intermissionEntities = new ArrayList<>();
    private final List<EntityType<? extends AbstractChunkLoadingEntity>> chosenEnemies = new ArrayList<>();

    private final List<AbstractUpgrade> chosenUpgrades = new ArrayList<>();
    public final Object2IntMap<UpgradeType<? extends AbstractUpgrade>> upgradeTypeCounts = new Object2IntArrayMap<>();

    private final List<ServerPlayer> playersFinished = new ArrayList<>();

    private List<UpgradeType<? extends AbstractUpgrade>> currentPool = new ArrayList<>();

    private GameController() {}

    @SubscribeEvent
    private static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (isVoidTunnels(event.getEntity().level()) && event.getEntity() instanceof ServerPlayer player && !INSTANCE.gameActive) {
            DimensionTransition transition = player.findRespawnPositionAndUseSpawnBlock(true, DimensionTransition.DO_NOTHING);
            event.getEntity().changeDimension(transition);
        }
    }

    @SubscribeEvent
    private static void onPlayerEnteredOrLeft(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (isVoidTunnels(event.getTo())) {
            ServerLevel gameLevel = event.getEntity().getServer().getLevel(event.getTo());
            INSTANCE.startGame(event.getEntity(), gameLevel);
        } else if (isVoidTunnels(event.getFrom())) {
            ServerLevel gameLevel = event.getEntity().getServer().getLevel(event.getFrom());
            for (AbstractUpgrade upgrade : INSTANCE.chosenUpgrades) {
                upgrade.removeFromPlayer(event.getEntity());
            }

        }
    }

    @SubscribeEvent
    private static void onDamage(LivingIncomingDamageEvent event) {
        if (isVoidTunnels(event.getEntity().level()) && event.getSource().equals(event.getEntity().level().damageSources().fall())) event.setCanceled(true);
    }

    @SubscribeEvent
    private static void onBreak(BlockEvent.BreakEvent event) {
        if (isVoidTunnels(event.getPlayer().level()) && !(event.getState().is(BlockRegistrar.GEMSTONE) && event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof PickaxeItem)) event.setCanceled(true);
    }

    @SubscribeEvent
    private static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (isVoidTunnels((Level) event.getLevel())) event.setCanceled(true);
    }

    @SubscribeEvent
    private static void onMultiPlace(BlockEvent.EntityMultiPlaceEvent event) {
        if (isVoidTunnels((Level) event.getLevel())) event.setCanceled(true);
    }

    @SubscribeEvent
    private static void onChunkLoad(ChunkEvent.Load event) {
        ChunkAccess access = event.getChunk();
        if (access instanceof LevelChunk chunk && chunk.getLevel() instanceof ServerLevel && isVoidTunnels(chunk.getLevel())) {
            long chunkId = INSTANCE.chunkInstanceMap.getOrDefault(chunk.getPos(), 0L);
            if (chunkId != INSTANCE.currentGameId) {
                INSTANCE.clearChunk((ServerLevel) chunk.getLevel(), chunk.getPos());
                INSTANCE.chunkInstanceMap.put(chunk.getPos(), INSTANCE.currentGameId);
            }
        }
    }

    @SubscribeEvent
    private static void onTick(LevelTickEvent.Pre event) {
        Level level = event.getLevel();
        if (isVoidTunnels(level)) {
            if (!level.isClientSide) {
                INSTANCE.tick((ServerLevel) level);
            }
        }
    }

    private void startGame(Player startingPlayer, ServerLevel level) {
        PacketDistributor.sendToPlayer((ServerPlayer) startingPlayer, new SetUpgradesPacket(List.of(upgradeTypeCounts.keySet().toArray(new UpgradeType<?>[0])), false));
        PacketDistributor.sendToPlayer((ServerPlayer) startingPlayer, new ResetClientUpgradesPacket());
        PacketDistributor.sendToPlayer((ServerPlayer) startingPlayer, new SetPredictedEnemiesPacket(List.copyOf(chosenEnemies)));

        for (AbstractUpgrade upgrade : chosenUpgrades) {
            upgrade.applyToPlayer(startingPlayer);
        }

        for (UpgradeType<?> type : upgradeTypeCounts.keySet()) {
            if (type.getUpgrade() instanceof IClientUpgrade) PacketDistributor.sendToPlayer((ServerPlayer) startingPlayer, new AddClientUpgrade(type));
        }

        if (gameActive) return;

        currentGameId = (long) (Math.random() * Long.MAX_VALUE);

        gamePrepped = true;
    }

    private void clearChunk(ServerLevel level, ChunkPos chunkPos) {
        LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
        LevelChunkSection[] sections = chunk.getSections();
        for (LevelChunkSection section : sections) {
            section.getStates().data = section.getStates().createOrReuseData(null, 0);
            section.getStates().data.palette().idFor(Blocks.AIR.defaultBlockState());
        }
        chunk.clearAllBlockEntities();
        chunk.setUnsaved(true);
    }

    private int maxGems(int level) {
        return (int) Math.ceil(50 + (Math.pow(level, 2) / 10) * 20);
    }

    private float maxEnemyPoints(int level) {
        return (float) Math.pow(level, 1.2f);
    }

    private void generateLevel(ServerLevel level) {
        Savete.LOGGER.info("Started generating level {}", this.currentLevel);

        BlockPos startPosition = new BlockPos(-10, 64, 5);
        BlockPos.MutableBlockPos currentBlockPos = new BlockPos.MutableBlockPos(-10, 64, 5);
        List<TileData> tiles = new ArrayList<>();
        TileRegistry.registryAccess().registryOrThrow(TileRegistry.TILE_DATA).entrySet().forEach(entry -> tiles.add(entry.getValue()));

        currentBlockPos.set(startPosition);

        rawGems = 0;
        totalRawGems = 0;
        int tilesGenerated = 0;
        int tilesWithoutGem = 0;
        List<TileData> templatesToPlace = new LinkedList<>();
        // pregenerate templates and clear chunks out in preparation (required for level clearing)
        while (totalRawGems < maxGems(this.currentLevel) && tilesGenerated < 500) {
            TileData data = tiles.get(random.nextInt(tiles.size()));
            StructureTemplate template = level.getStructureManager().get(data.structureLocation()).orElse(null);
            if (template == null) break;
            StructurePlaceSettings settings = new StructurePlaceSettings();
            List<StructureTemplate.StructureBlockInfo> info = template.filterBlocks(new BlockPos(1, 1, 1), settings, BlockRegistrar.GEMSTONE.get());
            if (info.isEmpty()) tilesWithoutGem++;
            else tilesWithoutGem = 0;

            // ensure tile generates with a gem every three tiles
            if (tilesWithoutGem >= 3) continue;

            totalRawGems += info.size();
            BlockPos placementPos = currentBlockPos.immutable().offset(data.tileOffset());

            templatesToPlace.add(data);
            tilesGenerated++;

            for (int j = 0; j < 5; j++) {
                if (random.nextDouble() < (1.0 / Math.pow(2, j)) * 2) {
                    GrapplePointEntity entity = new GrapplePointEntity(level);
                    entity.setPos(placementPos.getCenter().add(random.nextDouble() * 21, random.nextDouble() * 15 + 10, random.nextDouble() * 21));
                    entity.setInstanceNumber(currentGameId);
                    level.addFreshEntity(entity);
                }
            }

            currentBlockPos.move(data.tileMovement());
        }

        if (currentBlockPos.getZ() > furthestZ) furthestZ = currentBlockPos.getZ();

        for (int i = startPosition.getZ() - 32; i < furthestZ + 32; i += 16) {
            ChunkPos rightPos = new ChunkPos(new BlockPos(1, 1, i));
            ChunkPos leftPos = new ChunkPos(new BlockPos(-1, 1, i));
            clearChunk(level, rightPos);
            clearChunk(level, leftPos);
            level.players().forEach(p -> {
                p.connection.send(new ClientboundForgetLevelChunkPacket(rightPos));
                p.connection.send(new ClientboundForgetLevelChunkPacket(leftPos));
            });
        }

        Savete.LOGGER.info("Generated {} tiles for {} gems (quota {}), level {}", tilesGenerated, totalRawGems, maxGems(this.currentLevel), this.currentLevel);

        // reset location to start
        currentBlockPos.set(startPosition);

        // Actually place tiles
        for (TileData data : templatesToPlace) {
            StructureTemplate template = level.getStructureManager().get(data.structureLocation()).orElseThrow();

            StructurePlaceSettings settings = new StructurePlaceSettings();
            settings.addProcessor(new SaveteTileProcessor(this.getLevelPalette()));
            BlockPos placementPos = currentBlockPos.immutable().offset(data.tileOffset());

            template.placeInWorld(level, placementPos, placementPos, settings, createRandom(0L), 2);
            tilesGenerated++;

            currentBlockPos.move(data.tileMovement());
        }

        StructureTemplate template = level.getStructureManager().get(Savete.path("end")).orElseThrow();
        template.placeInWorld(level, currentBlockPos, currentBlockPos, new StructurePlaceSettings(), createRandom(0L), 2);

        createSaferoom(level, false);

        for (int i = startPosition.getZ() - 32; i < furthestZ + 32; i += 16) {
            ChunkPos rightPos =  new ChunkPos(0, SectionPos.blockToSectionCoord(i));
            ChunkPos leftPos =  new ChunkPos(-1, SectionPos.blockToSectionCoord(i));
            level.players().forEach(p -> {
                p.connection.send(new ClientboundLevelChunkWithLightPacket(level.getChunk(rightPos.x, rightPos.z), level.getLightEngine(), new BitSet(), new BitSet()));
                p.connection.send(new ClientboundLevelChunkWithLightPacket(level.getChunk(leftPos.x, leftPos.z), level.getLightEngine(), new BitSet(), new BitSet()));
            });

            // ensure they don't get cleared by assigning the correct id to the chunks
            chunkInstanceMap.put(rightPos, currentGameId);
            chunkInstanceMap.put(leftPos, currentGameId);
        }

        for (EntityType<? extends AbstractChunkLoadingEntity> type : chosenEnemies) {
            AbstractChunkLoadingEntity entity = type.create(level);

            entity.moveTo(0, 100, Math.random() * currentBlockPos.getZ() + 20);
            entity.setInstanceNumber(currentGameId);

            Savete.LOGGER.debug(entity.toString());

            level.addFreshEntity(entity);
        }

        PacketDistributor.sendToPlayersInDimension(level, new UpdateGemstoneCountPacket(gemstones, rawGems, totalRawGems, new Vector3f(Float.NaN)));
        PacketDistributor.sendToPlayersInDimension(level, new UpdateLevelInfoPacket(this.currentLevel, false));
        for (Entity entity : intermissionEntities) {
            entity.remove(Entity.RemovalReason.DISCARDED);
        }
        intermissionEntities.clear();
    }

    private void spawnUpgradeHolders(ServerLevel level) {
        Savete.LOGGER.debug("Coming into upgrade spawn with a pool size of {}", currentPool.size());

        for (int i = 0; i < 3; i++) {
            if (currentPool.isEmpty()) break;
            UpgradeHolderEntity entity = new UpgradeHolderEntity(level, currentPool.removeFirst());
            entity.setPos(-2.5 + i * 2, 66.5, -4);
            entity.setInstanceNumber(currentGameId);
            intermissionEntities.add(entity);
            level.addFreshEntity(entity);
        }

        Savete.LOGGER.debug("Finished spawning entities, now with a pool size of {}", currentPool.size());

        if (!currentPool.isEmpty()) {
            Savete.LOGGER.debug("Reroll active, current pool size {}", currentPool.size());
            UpgradeHolderEntity reroll = new UpgradeHolderEntity(level, UpgradeRegistry.REROLL.get());
            reroll.setPos(-2.5 + 3 * 2, 66.5, -4);
            reroll.setInstanceNumber(currentGameId);
            intermissionEntities.add(reroll);
            level.addFreshEntity(reroll);
        }
    }

    private void prepIntermission(ServerLevel level) {
        createSaferoom(level, true);

        PacketDistributor.sendToPlayersInDimension(level, new UpdateGemstoneCountPacket(gemstones, 0, 0, new Vector3f(Float.NaN)));

        SaveteBlockerEntity blocker = new SaveteBlockerEntity(level);
        blocker.setInstanceNumber(currentGameId);
        blocker.setPos(0.5, 67, 4);
        intermissionEntities.add(blocker);
        level.addFreshEntity(blocker);

        // Spawn intermission entities
        currentPool.clear();

        currentPool = new ArrayList<>(UpgradeRegistry.REGISTRY.getRegistry().get().entrySet().stream().map(Map.Entry::getValue)
                .filter(t -> this.currentLevel >= t.getMinLevel() && t.getRequiredUpgrades().keySet().stream().allMatch(h -> {
                    boolean predicate = t.getRequiredUpgrades().getOrDefault(h, -1) <= this.upgradeTypeCounts.getOrDefault(h.value(), -99);
                    Savete.LOGGER.debug("Filtering pool, checking req for {}; requires {} of {} and has {} (predicate returns {})", t.getTextureLocation(), t.getRequiredUpgrades().getOrDefault(h, -1), h.value().getTextureLocation(), this.upgradeTypeCounts.getOrDefault(h.value(), -99), predicate);
                    return predicate;
                }) && t.appearsInPool())
                .toList());

        for (UpgradeType<? extends AbstractUpgrade> upgrade : upgradeTypeCounts.keySet()) {
            if (upgradeTypeCounts.getOrDefault(upgrade, -1) >= upgrade.getMaxStack()) currentPool.remove(upgrade);
        }

        Collections.shuffle(currentPool);

        spawnUpgradeHolders(level);

        float enemyPoints = maxEnemyPoints(this.currentLevel);
        List<Map.Entry<DeferredHolder<EntityType<?>, ? extends EntityType<? extends AbstractChunkLoadingEntity>>, Float>> hostileEntries = new ArrayList<>(EntityRegistrar.HOSTILES.entrySet().stream().toList());
        Collections.shuffle(hostileEntries);
        chosenEnemies.clear();
        int idx = 0;
        while (enemyPoints > 0) {
            Map.Entry<DeferredHolder<EntityType<?>, ? extends EntityType<? extends AbstractChunkLoadingEntity>>, Float> enemy = hostileEntries.get(idx++);
            if (enemy.getValue() <= Math.ceil(enemyPoints)) {
                enemyPoints -= enemy.getValue();
                EntityType<? extends AbstractChunkLoadingEntity> type = enemy.getKey().get();
                chosenEnemies.add(type);
            }
            if (idx >= hostileEntries.size()) {
                Collections.shuffle(hostileEntries);
                idx = 0;
            }
        }

        PacketDistributor.sendToPlayersInDimension(level, new SetPredictedEnemiesPacket(List.copyOf(chosenEnemies)));

        level.players().forEach(p -> p.heal(p.getMaxHealth()));

        intermissionPrepped = true;
    }

    public void tick(ServerLevel level) {
        removeOutdatedEntities(level);
        for (Player player : level.players()) {
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setExhaustion(0);
            player.getFoodData().setSaturation(0);
            if (!player.getInventory().getArmor(3).is(ItemRegistrar.VOID_SUIT_HELMET)) player.hurt(level.damageSources().inWall(), 2);
        }
        if (level.players().isEmpty()) {
            INSTANCE.gamePrepped = false;
            INSTANCE.gameActive = false;
            INSTANCE.intermissionPrepped = false;
            INSTANCE.intermissionActive = false;
            INSTANCE.gemstones = 0;
            INSTANCE.currentLevel = 0;
            INSTANCE.chosenUpgrades.clear();
            INSTANCE.upgradeTypeCounts.clear();
            INSTANCE.chosenEnemies.clear();
            INSTANCE.furthestZ = 0;
            Iterable<Entity> entities = level.getEntities().getAll();
            List<Entity> toRemove = new ArrayList<>();
            entities.forEach(toRemove::add);
            for (Entity entity : toRemove) {
                if (entity == null) continue;
                if (!(entity instanceof Player)) entity.remove(Entity.RemovalReason.DISCARDED);
            }
        }

        if (gamePrepped && !intermissionActive && !gameActive) {
            generateLevel(level);

            gameActive = true;
        }
        if (gamePrepped && intermissionActive) {
            if (!intermissionPrepped) {
                prepIntermission(level);
            }

            if (intermissionReadied >= level.players().size()) {
                intermissionActive = false;
                intermissionPrepped = false;
            }
            return;
        }
        if (!gameActive) return;

        if (playersFinished.size() == level.players().size() && !playersFinished.isEmpty()) {
            for (ServerPlayer player : playersFinished) {
                PacketDistributor.sendToPlayer(player, new FinishLevelPacket(false));
            }
            nextLevel(level);
        }

        for (ServerPlayer player : playersFinished) {
            player.teleportTo(0, 256, 0);
        }
    }

    private void removeOutdatedEntities(ServerLevel level) {
        List<TemporaryGameplayEntity> gameplayEntities = new ArrayList<>();
        level.getAllEntities().forEach(entity -> {
            if (entity instanceof TemporaryGameplayEntity t) gameplayEntities.add(t);
        });
        gameplayEntities.forEach(temporaryGameplayEntity -> {
            if (temporaryGameplayEntity.getInstanceNumber() != currentGameId) {
                temporaryGameplayEntity.remove(Entity.RemovalReason.DISCARDED);
            }
        });
    }

    private void createSaferoom(Level level, boolean intermission) {
        // Base platform
        for (int i = 0; i < 81; i++) {
            level.setBlock(new BlockPos(i / 9 - 4, 64, i % 9 - 4), Blocks.STONE.defaultBlockState(), 2);
        }

        for (int i = -4; i < 4; i++) {
            for (int j = 65; j < 80; j++) {
                level.setBlock(new BlockPos(4, j, i), Blocks.STONE.defaultBlockState(), 2);
                level.setBlock(new BlockPos(-4, j, i), Blocks.STONE.defaultBlockState(), 2);
            }
        }

        for (int i = -4; i < 4; i++) {
            for (int j = 64; j < 80; j++) {
                level.setBlock(new BlockPos(i, j, -5), Blocks.BARRIER.defaultBlockState(), 2);
                if (j >= 69) level.setBlock(new BlockPos(i, j, 4), Blocks.STONE.defaultBlockState(), 2);
            }
        }

        for (int i = -4; i < 4; i++) {
            for (int j = 65; j <= 68; j++) {
                level.setBlock(new BlockPos(i, j, 4), intermission ? Blocks.BARRIER.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
            }
        }

        for (int i = -3; i <= 3; i++) {
            for (int j = -4; j <= 3; j++) {
                level.setBlock(new BlockPos(i, 80, j), Blocks.BARRIER.defaultBlockState(), 2);
            }
        }

        level.setBlock(new BlockPos(-3, 70, 2), BlockRegistrar.NEXT_LEVEL_INFO.get().defaultBlockState(), 2);
    }

    public void crackGemstone(Vec3 position, ServerLevel level) {
        if (isVoidTunnels(level)) {
            float gemsFromStone = random.nextIntBetweenInclusive(3, 7);
            for (AbstractUpgrade upgrade : chosenUpgrades) {
                gemsFromStone = upgrade.onGemstoneCollect(level, position, gemsFromStone);
            }
            gemstones += gemsFromStone;
            rawGems++;
            PacketDistributor.sendToPlayersInDimension(level, new UpdateGemstoneCountPacket(gemstones, rawGems, totalRawGems, position.toVector3f()));
        }
    }

    public void informPlayerReachedEnd(ServerLevel level, ServerPlayer player) {
        if (isVoidTunnels(level) && gameActive && !playersFinished.contains(player)) {
            playersFinished.add(player);

            PacketDistributor.sendToPlayer(player, new FinishLevelPacket(true));

            player.teleportTo(0, 256, 0);
        }
    }

    public void nextLevel(ServerLevel level) {
        if (isVoidTunnels(level) && gameActive) {
            Savete.LOGGER.info("Level complete!");
            currentGameId = (long) (Math.random() * Long.MAX_VALUE);
            gameActive = false;
            intermissionActive = true;
            intermissionPrepped = false;
            intermissionDelay = -1;
            intermissionReadied = 0;
            this.currentLevel++;
            PacketDistributor.sendToPlayersInDimension(level, new UpdateLevelInfoPacket(this.currentLevel, true));
            playersFinished.clear();
            level.players().forEach(p -> {
                p.teleportTo(0, 66, 0);
                // players sometimes don't show up, make sure they are properly added
                level.players().forEach(other -> {
                    if (other != p) other.connection.send(new ClientboundAddEntityPacket(p, 0, new BlockPos(0, 66, 0)));
                });
            });
        }
    }
    public void setReadiedPlayers(ServerLevel level, int num) {
        if (isVoidTunnels(level)) {
            intermissionReadied = num;
            if (num == level.players().size() && intermissionDelay == -1) {
                intermissionDelay = 10;
            }
        }
    }
    public void informUpgradeChosen(UpgradeHolderEntity entity) {
        if (isVoidTunnels(entity.level())) {
            UpgradeType<? extends AbstractUpgrade> type = entity.getUpgrade();
            if (type == UpgradeRegistry.REROLL.get()) {
                List<Entity> upgrades = List.copyOf(intermissionEntities.stream().filter(e -> e instanceof UpgradeHolderEntity).toList());
                for (Entity e : upgrades) {
                    System.out.println("removing " + e);
                    intermissionEntities.remove(e);
                    e.remove(Entity.RemovalReason.DISCARDED);
                }
                spawnUpgradeHolders((ServerLevel) entity.level());
                PacketDistributor.sendToPlayersInDimension((ServerLevel) entity.level(), new SpawnQuasarParticlePacket(Savete.path("purchase"), entity.getPosition(1).toVector3f().add(0, 0.5f, 0)));

                return;
            }

            float gemCost = type.getGemCost() * (float)Math.pow(type.getStackMultiplier(), upgradeTypeCounts.getOrDefault(type, 0));
            if (gemCost <= this.gemstones) {
                this.gemstones -= gemCost;
                PacketDistributor.sendToPlayersInDimension((ServerLevel) entity.level(), new UpdateGemstoneCountPacket(gemstones, 0, 0, new Vector3f(Float.NaN)));
                PacketDistributor.sendToPlayersInDimension((ServerLevel) entity.level(), new SetUpgradesPacket(List.of(type), true));
                PacketDistributor.sendToPlayersInDimension((ServerLevel) entity.level(), new SpawnQuasarParticlePacket(Savete.path("purchase"), entity.getPosition(1).toVector3f().add(0, 0.5f, 0)));

                AbstractUpgrade upgrade = type.getUpgrade();
                chosenUpgrades.add(upgrade);
                upgradeTypeCounts.computeInt(type, (t, i) -> {
                    if (i == null) return 1;
                    return i + 1;
                });
                for (Player player : entity.level().players()) {
                    upgrade.applyToPlayer(player);
                    if (upgrade instanceof IClientUpgrade) { // Client only accepts packets that implement IClientUpgrade
                        PacketDistributor.sendToPlayer((ServerPlayer) player, new AddClientUpgrade(type));
                    }
                }
                entity.remove(Entity.RemovalReason.DISCARDED);
                intermissionEntities.remove(entity);
                currentPool.remove(type);

                if (!currentPool.isEmpty()) {
                    UpgradeHolderEntity newEntity = new UpgradeHolderEntity(entity.level(), currentPool.removeFirst());
                    newEntity.setInstanceNumber(currentGameId);
                    newEntity.setPos(entity.getX(), entity.getY(), entity.getZ());
                    entity.level().addFreshEntity(newEntity);
                    intermissionEntities.add(newEntity);
                }
            }
        }
    }

    public boolean isIntermissionActive() {
        return intermissionActive;
    }

    public boolean isGameActive() {
        return gameActive;
    }

    public static boolean isVoidTunnels(ResourceLocation location) {
        return location.equals(Savete.path("void_tunnels"));
    }

    public static boolean isVoidTunnels(ResourceKey<Level> location) {
        return isVoidTunnels(location.location());
    }

    public static boolean isVoidTunnels(@Nullable Level level) {
        if (level == null) return false;

        return isVoidTunnels(level.dimension());
    }

    private Block getLevelPalette() {
        if (this.currentLevel > 20) {
            return Blocks.BEDROCK;
        } else if (this.currentLevel > 15) {
            return Blocks.NETHER_WART_BLOCK;
        } else if (this.currentLevel > 10) {
            return Blocks.NETHERRACK;
        } else if (this.currentLevel > 5) {
            return Blocks.RED_TERRACOTTA;
        } else {
            return Blocks.STONE;
        }
    }

    public static class SaveteTileProcessor extends StructureProcessor {
        public static final MapCodec<SaveteTileProcessor> CODEC;
        private final Block palette;
        public static StructureProcessorType<?> TYPE;

        private SaveteTileProcessor(Block palette) {
            this.palette = palette;
        }

        @Override
        public @Nullable StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo, StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings) {
            if (relativeBlockInfo.state().is(Blocks.STONE)) {
                return new StructureTemplate.StructureBlockInfo(relativeBlockInfo.pos(), this.palette.defaultBlockState(), relativeBlockInfo.nbt());
            }
            return super.processBlock(level, offset, pos, blockInfo, relativeBlockInfo, settings);
        }

        protected StructureProcessorType<?> getType() {
            return TYPE;
        }

        static {
            CODEC = BlockState.CODEC.xmap(BlockBehaviour.BlockStateBase::getBlock, Block::defaultBlockState).fieldOf("block").xmap(SaveteTileProcessor::new, (processor) -> processor.palette);
        }
    }
}
