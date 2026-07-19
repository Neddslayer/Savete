package dev.neddslayer.savete.entity.hostile;

import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.entity.AbstractChunkLoadingEntity;
import dev.neddslayer.savete.gameplay.upgrade.UpgradeType;
import dev.neddslayer.savete.network.SpawnQuasarParticlePacket;
import dev.neddslayer.savete.registrar.EntityRegistrar;
import foundry.veil.api.quasar.fx.Line;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SquelchEntity extends AbstractChunkLoadingEntity {
    public int jumpingTime = 60;
    public static final EntityDataAccessor<List<Vector3f>> JUMP_PATH = SynchedEntityData.defineId(SquelchEntity.class, EntityDataSerializer.forValueType(ByteBufCodecs.VECTOR3F.apply(ByteBufCodecs.list())));
    public static final EntityDataAccessor<Integer> RANDOM_DELAY =  SynchedEntityData.defineId(SquelchEntity.class, EntityDataSerializers.INT);
    public boolean landed = true;

    public SquelchEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public SquelchEntity(Level level) {
        super(EntityRegistrar.SQUELCH.get(), level);
    }

    @Override
    public void tick() {
        super.tick();

        if (jumpingTime >= 60 + this.entityData.get(RANDOM_DELAY)) {
            if (!level().isClientSide) {
                double x = this.getX() + (this.random.nextDouble() - 0.5) * 5.0;
                double y = this.getY() + (double) (this.random.nextInt(64));
                double z = this.getZ() + (this.random.nextDouble() - 0.45) * 20.0;
                BlockPos.MutableBlockPos teleportTarget = new BlockPos.MutableBlockPos(x, y, z);
                while (teleportTarget.getY() > this.level().getMinBuildHeight() && !this.level().getBlockState(teleportTarget.below()).blocksMotion()) {
                    teleportTarget.move(Direction.DOWN);
                }
                if (level().hasChunkAt(new BlockPos(teleportTarget)) && teleportTarget.getY() > this.level().getMinBuildHeight()) {
                    Line line = new Line(new Vec3[]{position(), lerp(0.5, position(), teleportTarget.getBottomCenter()).add(0, Math.max(position().distanceTo(teleportTarget.getBottomCenter()) * 2, 10), 0), teleportTarget.getBottomCenter()}, 0, a -> 0f);
                    line.setFrequency(10);
                    line.setCurveMode(Line.CurveMode.CATMULL_ROM);
                    Vec3[] jumpPath = line.setupCurvePoints();
                    jumpPath[jumpPath.length - 1] = teleportTarget.getBottomCenter();
                    List<Vector3f> path = Arrays.stream(jumpPath).filter(Objects::nonNull).map(Vec3::toVector3f).toList();
                    this.entityData.set(JUMP_PATH, path);
                    this.entityData.set(RANDOM_DELAY, this.random.nextInt(-10, 10));
                    jumpingTime = 0;
                    landed = false;
                }
            }
        } else {
            jumpingTime++;
            List<Vector3f> path = this.entityData.get(JUMP_PATH);
            if (jumpingTime < path.size()) {
                this.setPos(new Vec3(path.get(jumpingTime)));
            } else if (!this.landed) {
                this.landed = true;
                if (!level().isClientSide) {
                    PacketDistributor.sendToPlayersTrackingEntity(this, new SpawnQuasarParticlePacket(Savete.path("slam"), this.getPosition(1).toVector3f()));
                    level().getEntitiesOfClass(ServerPlayer.class, AABB.ofSize(this.getPosition(1), 20, 5, 20)).forEach(player -> {
                        double d = player.getPosition(1).distanceTo(this.getPosition(1));
                        if (d < 10) {
                            player.hurt(level().damageSources().generic(), (float) ((1 / d) * 10));
                            player.push(player.getPosition(1).subtract(this.getPosition(1)).add(0, 1, 0).normalize().multiply((1 / d) * 15, (1 / d) * 15, (1 / d) * 15));
                            player.hasImpulse = true;
                        }
                    });
                }
            }
        }
    }

    private Vec3 lerp(double delta, Vec3 start, Vec3 end) {
        return new Vec3(
                Mth.lerp(delta, start.x, end.x),
                Mth.lerp(delta, start.y, end.y),
                Mth.lerp(delta, start.z, end.z)
        );
    }

    @Override
    public void playerTouch(Player player) {
        super.playerTouch(player);
        player.hurt(level().damageSources().generic(), 6);
    }

    @Override
    public void onSyncedDataUpdated(List<SynchedEntityData.DataValue<?>> dataValues) {
        super.onSyncedDataUpdated(dataValues);
        this.jumpingTime = 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(JUMP_PATH, List.of());
        builder.define(RANDOM_DELAY, 0);
    }
}
