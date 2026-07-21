package dev.neddslayer.savete.entity.hostile;

import dev.neddslayer.savete.entity.AbstractChunkLoadingEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Vector2d;

import java.util.List;

public class HaloEntity extends AbstractChunkLoadingEntity {
    public static final EntityDataAccessor<Integer> ENTITY_TARGET = SynchedEntityData.defineId(HaloEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> RANDOM_DELAY =  SynchedEntityData.defineId(HaloEntity.class, EntityDataSerializers.INT);
    public int attackTimer = 80;

    public HaloEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attackTimer > 120 + this.entityData.get(RANDOM_DELAY)) {
            if (!level().isClientSide) {
                ServerLevel level = (ServerLevel) level();
                ServerPlayer target = level.getRandomPlayer();
                if (target == null) return;

                this.entityData.set(ENTITY_TARGET, target.getId(), true);
                this.entityData.set(RANDOM_DELAY, this.random.nextIntBetweenInclusive(-20, 40));

                this.attackTimer = 0;
            }
        } else {
            if (this.attackTimer < 40) {
                Entity target = level().getEntity(this.entityData.get(ENTITY_TARGET));
                if (target != null) {
                    this.setPos(target.getPosition(0));
                }
            }
            if (this.attackTimer >= 70) {
                List<Entity> entities = this.level().getEntities(this, AABB.ofSize(this.getPosition(0).add(0, 2, 0), 8, 4, 8), EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE));
                for (Entity entity : entities) {
                    if (new Vector2d(entity.getX(), entity.getZ()).distance(this.getPosition(0).x, this.getPosition(0).z) < 4) {
                        entity.hurt(level().damageSources().generic(), 8);
                    }
                }
            }
            this.attackTimer++;
        }
    }

    @Override
    public void onSyncedDataUpdated(List<SynchedEntityData.DataValue<?>> dataValues) {
        super.onSyncedDataUpdated(dataValues);
        this.attackTimer = 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ENTITY_TARGET, Integer.MIN_VALUE);
        builder.define(RANDOM_DELAY, 0);
    }
}
