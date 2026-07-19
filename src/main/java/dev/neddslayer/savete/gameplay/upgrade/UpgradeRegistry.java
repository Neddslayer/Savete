package dev.neddslayer.savete.gameplay.upgrade;

import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.gameplay.upgrade.api.AbstractUpgrade;
import dev.neddslayer.savete.gameplay.upgrade.impl.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

import static dev.neddslayer.savete.Savete.MODID;

public class UpgradeRegistry {
    public static final ResourceKey<Registry<UpgradeType<? extends AbstractUpgrade>>> UPGRADE_TYPE = ResourceKey.createRegistryKey(Savete.path("upgrade_type"));
    public static final DeferredRegister<UpgradeType<? extends AbstractUpgrade>> REGISTRY = DeferredRegister.create(UPGRADE_TYPE, MODID);

    // Attributes
    public static final DeferredHolder<UpgradeType<? extends AbstractUpgrade>, UpgradeType<AttributeUpgrade>>     SPEED              = register("speed",          () -> UpgradeType.Builder.of(() -> new AttributeUpgrade(Attributes.MOVEMENT_SPEED, 0.1f, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)).location(Savete.path("speed")).maxStack(3).gemCost(200).minLevel(1).build());
    public static final DeferredHolder<UpgradeType<? extends AbstractUpgrade>, UpgradeType<AttributeUpgrade>>     SUPER_SPEED        = register("super_speed",    () -> UpgradeType.Builder.of(() -> new AttributeUpgrade(Attributes.MOVEMENT_SPEED, 0.5f, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)).location(Savete.path("super_speed")).gemCost(1000).minLevel(10).addRequiredUpgrade(SPEED, 3).build());
    public static final DeferredHolder<UpgradeType<? extends AbstractUpgrade>, UpgradeType<AttributeUpgrade>>     JUMP               = register("jump",           () -> UpgradeType.Builder.of(() -> new AttributeUpgrade(Attributes.JUMP_STRENGTH, 0.25f, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)).location(Savete.path("jump")).maxStack(2).gemCost(500).minLevel(1).build());
    public static final DeferredHolder<UpgradeType<? extends AbstractUpgrade>, UpgradeType<AttributeUpgrade>>     LOW_GRAV           = register("lowgrav",        () -> UpgradeType.Builder.of(() -> new AttributeUpgrade(Attributes.GRAVITY, -0.01f, AttributeModifier.Operation.ADD_VALUE)).location(Savete.path("lowgrav")).maxStack(2).gemCost(300).minLevel(5).build());
    public static final DeferredHolder<UpgradeType<? extends AbstractUpgrade>, UpgradeType<AttributeUpgrade>>     HEALTH_UP          = register("health_up",      () -> UpgradeType.Builder.of(() -> new AttributeUpgrade(Attributes.MAX_HEALTH, 4.0f, AttributeModifier.Operation.ADD_VALUE)).location(Savete.path("health_up")).maxStack(5).gemCost(750).stackMultiplier(1.5f).minLevel(5).build());
    public static final DeferredHolder<UpgradeType<? extends AbstractUpgrade>, UpgradeType<AttributeUpgrade>>     LONGER_REACH       = register("longer_reach",   () -> UpgradeType.Builder.of(() -> new AttributeUpgrade(Attributes.BLOCK_INTERACTION_RANGE, 1.0f, AttributeModifier.Operation.ADD_VALUE)).location(Savete.path("longer_reach")).maxStack(5).gemCost(600).stackMultiplier(1.25f).minLevel(3).build());
    // Server
    public static final DeferredHolder<UpgradeType<? extends AbstractUpgrade>, UpgradeType<GemMultiplierUpgrade>> GEM_MULTIPLIER     = register("gem_multiplier", () -> UpgradeType.Builder.of(GemMultiplierUpgrade::new).location(Savete.path("gem_multiplier")).gemCost(250).maxStack(3).stackMultiplier(2.0f).minLevel(1).build());
    public static final DeferredHolder<UpgradeType<? extends AbstractUpgrade>, UpgradeType<GemCollectorUpgrade>>  GEM_COLLECTOR      = register("gem_collector",  () -> UpgradeType.Builder.of(GemCollectorUpgrade::new).location(Savete.path("gem_collector")).gemCost(800).maxStack(2).stackMultiplier(3.0f).minLevel(10).addRequiredUpgrade(GEM_MULTIPLIER, 2).build());
    // Client
    public static final DeferredHolder<UpgradeType<? extends AbstractUpgrade>, UpgradeType<GemCounterUpgrade>>    GEM_COUNTER        = register("gem_counter",    () -> UpgradeType.Builder.of(GemCounterUpgrade::new).location(Savete.path("gem_counter")).gemCost(300).minLevel(1).build());
    public static final DeferredHolder<UpgradeType<? extends AbstractUpgrade>, UpgradeType<DoubleJumpUpgrade>>    DOUBLE_JUMP        = register("double_jump",    () -> UpgradeType.Builder.of(DoubleJumpUpgrade::new).location(Savete.path("double_jump")).gemCost(750).minLevel(3).build());
    public static final DeferredHolder<UpgradeType<? extends AbstractUpgrade>, UpgradeType<FastFallUpgrade>>      FAST_FALL          = register("fast_fall",      () -> UpgradeType.Builder.of(FastFallUpgrade::new).location(Savete.path("fast_fall")).gemCost(250).minLevel(1).build());

    public static final DeferredHolder<UpgradeType<? extends AbstractUpgrade>, UpgradeType<EmptyUpgrade>>         REROLL             = register("reroll",         () -> UpgradeType.Builder.of(EmptyUpgrade::new).location(Savete.path("reroll")).setAppearsInPool(false).build());

    @ApiStatus.Internal
    public static void bootstrap(IEventBus bus) {
        REGISTRY.makeRegistry(builder -> builder.sync(true));
        REGISTRY.register(bus);
    }

    private static <T extends UpgradeType<? extends AbstractUpgrade>> DeferredHolder<UpgradeType<? extends AbstractUpgrade>, T> register(String name, Supplier<T> upgrade) {
        return REGISTRY.register(name, upgrade);
    }
}
