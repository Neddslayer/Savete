package dev.neddslayer.savete.entity.hostile;

import dev.neddslayer.savete.entity.AbstractChunkLoadingEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

public class TantrumEntity extends AbstractChunkLoadingEntity {
    public static final EntityDataAccessor<Vector3f> TARGET_DIRECTION = SynchedEntityData.defineId(TantrumEntity.class, EntityDataSerializers.VECTOR3);
    public static final EntityDataAccessor<Integer> RANDOM_DELAY =  SynchedEntityData.defineId(TantrumEntity.class, EntityDataSerializers.INT);
    public int attackTimer = 20;

    public TantrumEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attackTimer > 20 + this.entityData.get(RANDOM_DELAY)) {
            if (!level().isClientSide) {
                ServerLevel level = (ServerLevel) level();
                ServerPlayer target = (ServerPlayer) level.getNearestPlayer(this, Double.MAX_VALUE);
                if (target == null) return;

                Vector3f direction = target.getPosition(0).subtract(this.getPosition(0)).normalize().toVector3f();
                this.entityData.set(TARGET_DIRECTION, direction);
                this.entityData.set(RANDOM_DELAY, this.random.nextIntBetweenInclusive(-5, 20));

                this.attackTimer = 0;
            }
        } else {
            if (this.attackTimer >= 10 && this.entityData.get(TARGET_DIRECTION).length() > 0) {
                double factor = (1.0 - Math.clamp((this.attackTimer - 10) / 10f, 0, 1)) * 2;
                Vec3 newPos = this.getPosition(0).add(new Vec3(this.entityData.get(TARGET_DIRECTION)).multiply(factor, factor, factor));
                this.setPos(newPos);
                if (this.level() instanceof ServerLevel serverLevel) {
                    Player nearest = serverLevel.getNearestPlayer(this, 1.5);
                    if (nearest != null) {
                        nearest.hurt(serverLevel.damageSources().generic(), 8);
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
        builder.define(TARGET_DIRECTION, new Vector3f());
        builder.define(RANDOM_DELAY, 0);
    }
}
