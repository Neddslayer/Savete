package dev.neddslayer.savete.mixin;

import dev.neddslayer.savete.Savete;
import dev.neddslayer.savete.gameplay.movement.GrappleController;
import dev.neddslayer.savete.registrar.ItemRegistrar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.NumberFormat;

@Mixin(PlayerModel.class)
public class PlayerModelMixin<T extends LivingEntity> extends HumanoidModel<T> {

    @Shadow
    @Final
    public ModelPart rightSleeve;

    public PlayerModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void injectHookPos(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof Player player && player.getItemInHand(InteractionHand.MAIN_HAND).is(ItemRegistrar.GRAPPLING_HOOK)) {
            Vec3 hook = GrappleController.GRAPPLING_PLAYERS.getOrDefault(player.getUUID(), null);
            if (hook != null && !Double.isNaN(hook.x)) {
                float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);

                Vector3f rightArmPos = new Vector3f(this.rightArm.x / 16f, this.rightArm.y / 16, this.rightArm.z / 16);
                rightArmPos.add(player.getPosition(partialTick).toVector3f());

                Vector3f forwardVector = hook.toVector3f().sub(rightArmPos, new Vector3f()).normalize();
                Quaternionf blingle = new Quaternionf().lookAlong(forwardVector, new Vector3f(0, 1, 0));
                blingle.rotateY(-player.getPreciseBodyRotation(partialTick) * Mth.DEG_TO_RAD);
                Vector3f rot = blingle.getEulerAnglesXYZ(new Vector3f());
                this.rightArm.xRot = -rot.x + (Mth.HALF_PI);
                this.rightArm.yRot = rot.y;
                this.rightArm.zRot = -rot.z;
                this.rightSleeve.copyFrom(this.rightArm);
            }
        }
    }
}
