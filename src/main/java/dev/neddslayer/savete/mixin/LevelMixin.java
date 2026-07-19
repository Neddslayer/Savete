package dev.neddslayer.savete.mixin;

import dev.neddslayer.savete.Savete;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public abstract class LevelMixin {

    @Shadow
    public abstract ResourceKey<Level> dimension();

    @Inject(method = "close", at = @At("HEAD"), cancellable = true)
    public void stopClose(CallbackInfo ci) {
        if (this.dimension().location().equals(Savete.path("void_tunnels"))) ci.cancel();
    }
}
