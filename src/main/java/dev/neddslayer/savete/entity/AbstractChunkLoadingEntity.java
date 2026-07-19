package dev.neddslayer.savete.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Comparator;

public abstract class AbstractChunkLoadingEntity extends TemporaryGameplayEntity {
    public static final TicketType<ChunkPos> SAVETE_ENTITY_TICKET = TicketType.create("savete_entity", Comparator.comparingLong(ChunkPos::toLong), 40);
    private long chunkTicketExpiryTicks = 0L;
    protected int chunkTicketDistance = 2;

    public AbstractChunkLoadingEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel serverLevel && this.isAlive() && --this.chunkTicketExpiryTicks <= 0) {
            this.chunkTicketExpiryTicks = addChunkTicket(serverLevel, blockPosition());
        }
    }

    private long addChunkTicket(ServerLevel serverLevel, BlockPos pos) {
        serverLevel.resetEmptyTime();
        serverLevel.getChunkSource().addRegionTicket(SAVETE_ENTITY_TICKET, new ChunkPos(pos), chunkTicketDistance, new ChunkPos(pos), true);
        return SAVETE_ENTITY_TICKET.timeout() - 1L;
    }

    protected void forceAddChunkTicket(BlockPos pos) {
        if (level() instanceof ServerLevel serverLevel && this.isAlive()) {
            this.chunkTicketExpiryTicks = addChunkTicket(serverLevel, pos);
        }
    }

    @Override
    public void moveTo(double x, double y, double z, float yRot, float xRot) {
        super.moveTo(x, y, z, yRot, xRot);
        if (level() instanceof ServerLevel serverLevel && this.isAlive()) {
            this.chunkTicketExpiryTicks = this.addChunkTicket(serverLevel, BlockPos.containing(x, y, z));
        }
    }
}
