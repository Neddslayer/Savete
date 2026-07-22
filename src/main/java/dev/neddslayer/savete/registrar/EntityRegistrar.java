package dev.neddslayer.savete.registrar;

import dev.neddslayer.savete.entity.*;
import dev.neddslayer.savete.entity.hostile.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;

import static dev.neddslayer.savete.Savete.MODID;

public class EntityRegistrar {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<RiftEntity>> RIFT = ENTITIES.register("void_rift", () -> EntityType.Builder.<RiftEntity>of(RiftEntity::new, MobCategory.MISC).sized(0.1f, 0.1f).build("void_rift"));
    public static final DeferredHolder<EntityType<?>, EntityType<GrapplePointEntity>> GRAPPLE_POINT = ENTITIES.register("grapple_point", () -> EntityType.Builder.<GrapplePointEntity>of(GrapplePointEntity::new, MobCategory.MISC).sized(1.1f, 1.1f).build("grapple_point"));
    public static final DeferredHolder<EntityType<?>, EntityType<SaveteBlockerEntity>> BLOCKER = ENTITIES.register("blocker", () -> EntityType.Builder.<SaveteBlockerEntity>of(SaveteBlockerEntity::new, MobCategory.MISC).sized(0.1f, 0.1f).build("blocker"));
    public static final DeferredHolder<EntityType<?>, EntityType<UpgradeHolderEntity>> UPGRADE_HOLDER = ENTITIES.register("upgrade_holder", () -> EntityType.Builder.<UpgradeHolderEntity>of(UpgradeHolderEntity::new, MobCategory.MISC).sized(1.0f, 1.0f).build("upgrade_holder"));

    public static final DeferredHolder<EntityType<?>, EntityType<SquelchEntity>> SQUELCH = ENTITIES.register("squelch", () -> EntityType.Builder.<SquelchEntity>of(SquelchEntity::new, MobCategory.MISC).sized(1f, 1f).build("squelch"));
    public static final DeferredHolder<EntityType<?>, EntityType<HeliosEntity>> HELIOS = ENTITIES.register("helios", () -> EntityType.Builder.of(HeliosEntity::new, MobCategory.MISC).sized(1f, 1f).build("helios"));
    public static final DeferredHolder<EntityType<?>, EntityType<TantrumEntity>> TANTRUM = ENTITIES.register("tantrum", () -> EntityType.Builder.of(TantrumEntity::new, MobCategory.MISC).sized(1f, 1f).build("tantrum"));
    public static final DeferredHolder<EntityType<?>, EntityType<HaloEntity>> HALO = ENTITIES.register("halo", () -> EntityType.Builder.of(HaloEntity::new, MobCategory.MISC).sized(0.01f, 0.01f).build("halo"));
    public static final DeferredHolder<EntityType<?>, EntityType<OrbitEntity>> ORBIT = ENTITIES.register("orbit", () -> EntityType.Builder.of(OrbitEntity::new, MobCategory.MISC).sized(0.25f, 0.25f).build("orbit"));

    public static final Map<DeferredHolder<EntityType<?>, ? extends EntityType<? extends AbstractChunkLoadingEntity>>, Float> HOSTILES = Map.of(
            SQUELCH, 0.1f,
            HELIOS, 4.5f,
            TANTRUM, 2.5f,
            HALO, 0.5f
            //ORBIT, 3.5f
    );

    public static void bootstrap(IEventBus bus) {
        ENTITIES.register(bus);
    }
}
