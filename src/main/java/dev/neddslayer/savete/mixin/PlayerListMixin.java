package dev.neddslayer.savete.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.neddslayer.savete.gameplay.GameController.isVoidTunnels;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Redirect(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/DimensionTransition$PostDimensionTransition;)Lnet/minecraft/world/level/portal/DimensionTransition;"))
    private DimensionTransition voidtunnels$ensureInventory(ServerPlayer instance, boolean keepInventory, DimensionTransition.PostDimensionTransition postDimensionTransition) {
        if (isVoidTunnels(instance.level())) {
            return instance.findRespawnPositionAndUseSpawnBlock(true, postDimensionTransition);
        }
        return instance.findRespawnPositionAndUseSpawnBlock(keepInventory, postDimensionTransition);
    }

    @Redirect(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V"))
    private void voidtunnels$ensureInventory(ServerPlayer instance, ServerPlayer that, boolean keepEverything) {
        if (isVoidTunnels(that.level())) {
            instance.restoreFrom(that, true);
        }
        instance.restoreFrom(that, keepEverything);
    }
}
