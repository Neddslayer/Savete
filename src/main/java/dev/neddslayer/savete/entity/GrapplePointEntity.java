package dev.neddslayer.savete.entity;

import dev.neddslayer.savete.gameplay.LocalPlayerManager;
import dev.neddslayer.savete.gameplay.movement.GrappleController;
import dev.neddslayer.savete.gameplay.upgrade.api.IClientUpgrade;
import dev.neddslayer.savete.registrar.EntityRegistrar;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import static java.lang.Math.clamp;

public class GrapplePointEntity extends TemporaryGameplayEntity {
    public int age;
    private int cooldown;

    public GrapplePointEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public GrapplePointEntity(Level level) {
        super(EntityRegistrar.GRAPPLE_POINT.get(), level);
    }

    @Override
    public void playerTouch(Player player) {
        super.playerTouch(player);
        if (cooldown <= 0) {
            player.setDeltaMovement(player.getDeltaMovement().x * 1.1, 1.1, player.getDeltaMovement().z * 1.1);
            if (player instanceof LocalPlayer) {
                GrappleController.INSTANCE.hookCooldown = clamp(GrappleController.INSTANCE.hookCooldown + 20, 0, 100);
                GrappleController.INSTANCE.cancelGrapple();
                for (IClientUpgrade upgrade : LocalPlayerManager.clientUpgrades) {
                    upgrade.onHitGrapplePoint();
                }
            }
            cooldown = 4;
        }
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        if (cooldown > 0) cooldown--;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }
}
