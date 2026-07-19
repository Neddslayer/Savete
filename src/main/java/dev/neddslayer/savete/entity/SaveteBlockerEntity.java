package dev.neddslayer.savete.entity;

import dev.neddslayer.savete.gameplay.GameController;
import dev.neddslayer.savete.registrar.EntityRegistrar;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveteBlockerEntity extends TemporaryGameplayEntity {
    private final Map<Player, Vec3> capturedPlayers = new HashMap<>();

    public SaveteBlockerEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public SaveteBlockerEntity(Level level) {
        super(EntityRegistrar.BLOCKER.get(), level);
    }

    @Override
    public void tick() {
        level().getEntitiesOfClass(Player.class, AABB.ofSize(this.getPosition(0), 7, 4, 0.1)).forEach(p -> {
            capturedPlayers.putIfAbsent(p, p.getPosition(0));
        });
        for(Map.Entry<Player, Vec3> player : capturedPlayers.entrySet()) {
            if (player.getKey().isShiftKeyDown()) {
                capturedPlayers.remove(player.getKey());
                continue;
            }
            player.getKey().setPos(player.getValue());
            player.getKey().setDeltaMovement(0, 0, 0);
        }
        if (!level().isClientSide) {
            GameController.INSTANCE.setReadiedPlayers((ServerLevel) level(), capturedPlayers.size());
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    public Map<Player, Vec3> getCapturedPlayers() {
        return capturedPlayers;
    }
}
