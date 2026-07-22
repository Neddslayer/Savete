package dev.neddslayer.savete.entity.hostile;

import dev.neddslayer.savete.entity.AbstractChunkLoadingEntity;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class OrbitEntity extends AbstractChunkLoadingEntity {
    public Vec3 velocity = Vec3.ZERO;
    public int age;

    public OrbitEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    @Override
    public void tick() {
        super.tick();
        this.age++;
        Level level = level();
        Player target = level.getNearestPlayer(this, Double.MAX_VALUE);
        if (target == null) return;

        Vec3 dir = target.getPosition(0).subtract(this.getPosition(0)).normalize();

        velocity = velocity.add(dir.scale(2));
        if (velocity.length() > 8) velocity = velocity.normalize().scale(8);

        this.move(MoverType.SELF, velocity);

        for (Player player : level.players()) {
            if (this.getBoundingBox().intersects(player.getBoundingBox())) {
                player.hurt(level.damageSources().generic(), 2);
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }
}
