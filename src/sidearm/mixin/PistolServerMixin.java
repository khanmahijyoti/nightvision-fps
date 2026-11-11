package sidearm.mixin;

import sidearm.PistolLoadout;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class PistolServerMixin {
    @Inject(method = "tickServer", at = @At("TAIL"))
    private void sidearm$starter(CallbackInfo ci) { PistolLoadout.tick((MinecraftServer)(Object)this); }
}
