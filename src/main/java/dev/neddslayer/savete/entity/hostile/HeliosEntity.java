package dev.neddslayer.savete.entity.hostile;

import dev.neddslayer.savete.entity.AbstractChunkLoadingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class HeliosEntity extends AbstractChunkLoadingEntity {
    public static final EntityDataAccessor<Vector3f> TARGET_POSITION = SynchedEntityData.defineId(HeliosEntity.class, EntityDataSerializers.VECTOR3);
    public static final EntityDataAccessor<Vector3f> TARGET_BEAM_POSITION = SynchedEntityData.defineId(HeliosEntity.class, EntityDataSerializers.VECTOR3);
    public static final EntityDataAccessor<Integer> RANDOM_DELAY =  SynchedEntityData.defineId(HeliosEntity.class, EntityDataSerializers.INT);
    public int attackTimer = 0;

    public HeliosEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.chunkTicketDistance = 4;
    }

    @Override
    public void tick() {
        super.tick();
        if (attackTimer > 40 + this.entityData.get(RANDOM_DELAY)) {
            if (!level().isClientSide) {
                ServerLevel level = (ServerLevel) level();
                ServerPlayer target = level.getRandomPlayer();
                if (target == null) return;

                float radius = this.random.nextIntBetweenInclusive(15, 30);

                float y = (float) (target.getY() + (this.random.nextFloat()) * radius / 4);
                Vec3 direction = target.getLookAngle().multiply(8, 8, 8);

                Vector3f selfPosition = new Vector3f(
                        (float) (target.getX() + direction.x + (this.random.nextFloat() - 0.5) * radius),
                        y,
                        (float) (target.getZ() + direction.z + (this.random.nextFloat() - 0.5) * radius)
                );

                Vector3f targetPosition = new Vector3f(
                        (float) (target.getX() + direction.x + (this.random.nextFloat() - 0.5) * radius / 2),
                        y,
                        (float) (target.getZ() + direction.z + (this.random.nextFloat() - 0.5) * radius / 2)
                );

                this.entityData.set(TARGET_POSITION, selfPosition);
                this.entityData.set(TARGET_BEAM_POSITION, targetPosition);
                this.entityData.set(RANDOM_DELAY, this.random.nextIntBetweenInclusive(-10, 40));

                this.attackTimer = 0;
            }
        } else {
            this.attackTimer++;

            Vec3 currentPosition = this.getPosition(1);
            Vec3 targetPosition = new Vec3(this.entityData.get(TARGET_POSITION));
            Vec3 target = lerp(0.25, currentPosition, targetPosition);
            if (targetPosition.subtract(currentPosition).length() > 16) {
                // force chunk ticket creation to keep it loaded if it goes too far
                this.forceAddChunkTicket(BlockPos.containing(target));
            }
            this.setPos(target);

            if (this.attackTimer > 10 && this.attackTimer <= 30 && this.entityData.get(HeliosEntity.TARGET_BEAM_POSITION).length() > 0.1f) {
                Vector3f start = this.entityData.get(HeliosEntity.TARGET_POSITION);
                Vector3f endTarget = this.entityData.get(HeliosEntity.TARGET_BEAM_POSITION);
                Vector3f normal = endTarget.sub(start, new Vector3f()).normalize();
                Vector3f rotateAxis = new Vector3f(0, 1, 0).cross(normal).normalize();

                Vector3f dir = this.entityData.get(HeliosEntity.TARGET_BEAM_POSITION).sub(start, new Vector3f()).normalize().mul(30);
                dir.rotate(new Quaternionf().rotateAxis(Math.clamp((this.attackTimer - 10) / 20f, 0, 1) * Mth.PI - Mth.HALF_PI, rotateAxis));
                Vector3f end = start.add(dir, new Vector3f());

                EntityHitResult result = ProjectileUtil.getEntityHitResult(level(), this, new Vec3(start), new Vec3(end), AABB.ofSize(this.getPosition(1), 60, 60, 60), p -> true, 0.5f);

                if (result != null && result.getType() == HitResult.Type.ENTITY) {
                    result.getEntity().hurt(level().damageSources().generic(), 6);
                    result.getEntity().igniteForSeconds(2);
                }

                for (float i = 0; i < 1; i += 0.05f) {
                    Vector3f particle = start.lerp(end, i + this.random.nextFloat() * 0.05f, new Vector3f());
                    level().addParticle(ParticleTypes.ANGRY_VILLAGER, particle.x + this.random.nextFloat() - 0.5, particle.y + this.random.nextFloat() - 0.5, particle.z + this.random.nextFloat() - 0.5, 0, 0, 0);
                }
            }
        }
    }

    @Override
    public void moveTo(double x, double y, double z, float yRot, float xRot) {
        super.moveTo(x, y, z, yRot, xRot);
        this.entityData.set(TARGET_POSITION, new Vector3f((float) x, (float) y, (float) z));
    }

    private Vec3 lerp(double delta, Vec3 start, Vec3 end) {
        return new Vec3(
                Mth.lerp(delta, start.x, end.x),
                Mth.lerp(delta, start.y, end.y),
                Mth.lerp(delta, start.z, end.z)
        );
    }

    @Override
    public void onSyncedDataUpdated(List<SynchedEntityData.DataValue<?>> dataValues) {
        super.onSyncedDataUpdated(dataValues);
        this.attackTimer = 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TARGET_POSITION, this.getPosition(1).toVector3f());
        builder.define(TARGET_BEAM_POSITION, new Vector3f());
        builder.define(RANDOM_DELAY, 0);
    }
}
