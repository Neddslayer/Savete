package dev.neddslayer.savete.mixin;

import dev.neddslayer.savete.Savete;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {

    @Shadow
    @Final
    public ServerLevel level;

    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    public void stopSave(boolean flush, CallbackInfo ci) {
        if (this.level.dimension().location().equals(Savete.path("void_tunnels"))) {
            System.out.println("stopped save");
            //ci.cancel();
        }
    }

}
