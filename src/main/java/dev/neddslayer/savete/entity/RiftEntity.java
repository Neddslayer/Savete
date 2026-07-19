package dev.neddslayer.savete.entity;

import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.gameplay.GameController;
import dev.neddslayer.savete.registrar.EntityRegistrar;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.quasar.particle.ParticleEmitter;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class RiftEntity extends Entity {
    private static final EntityDataAccessor<Direction> FACE_DIRECTION;
    private ParticleEmitter emitter;

    public RiftEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public RiftEntity(Level level, Direction face) {
        super(EntityRegistrar.RIFT.get(), level);
        this.entityData.set(FACE_DIRECTION, face);
    }

    @Override
    public void tick() {
        if (this.level().isClientSide() && emitter.isRemoved()) {
            emitter = VeilRenderSystem.renderer().getParticleManager().createEmitter(Savete.path("rift_enter"));
            emitter.setPosition(getPosition(0));
            VeilRenderSystem.renderer().getParticleManager().addParticleSystem(emitter);
        }
        if (!this.level().isClientSide() && (!GameController.INSTANCE.isGameActive() || GameController.INSTANCE.currentLevel == 0)) {
            this.level().getEntities(this, this.getBoundingBox().inflate(0.1), EntitySelector.NO_SPECTATORS.and(e -> e instanceof Player)).forEach(player ->
                player.changeDimension(new DimensionTransition(this.level().getServer().getLevel(GameController.VOID_TUNNELS_KEY), new Vec3(0, 66, 0), Vec3.ZERO, 0, 0, e -> {}))
            );
        }
    }

    @Override
    public void playerTouch(Player player) {

    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FACE_DIRECTION, Direction.UP);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.entityData.set(FACE_DIRECTION, Direction.valueOf(compoundTag.getString("Face")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putString("Face", this.entityData.get(FACE_DIRECTION).name());
    }

    public Vector3dc getNormal() {
        Direction face = this.entityData.get(FACE_DIRECTION);
        return new Vector3d(face.getNormal().getX(), face.getNormal().getY(), face.getNormal().getZ());
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (this.level().isClientSide()) {
            emitter = VeilRenderSystem.renderer().getParticleManager().createEmitter(Savete.path("rift_enter"));
            emitter.setPosition(getPosition(0));
            VeilRenderSystem.renderer().getParticleManager().addParticleSystem(emitter);
        }
    }

    @Override
    public void onClientRemoval() {
        super.onClientRemoval();
        emitter.remove();
    }

    static {
        FACE_DIRECTION = SynchedEntityData.defineId(RiftEntity.class, EntityDataSerializers.DIRECTION);
    }
}
