package dev.neddslayer.savete.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public abstract class TemporaryGameplayEntity extends Entity {
    private long instanceNumber = 0;

    public TemporaryGameplayEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public long getInstanceNumber() {
        return this.instanceNumber;
    }

    public void setInstanceNumber(long instanceNumber) {
        this.instanceNumber = instanceNumber;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.instanceNumber = compoundTag.getLong("InstanceNumber");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putLong("InstanceNumber", this.instanceNumber);
    }
}
