package sidearm.mixin;

import sidearm.PistolClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class PistolInputMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void sidearm$tick(CallbackInfo ci) { PistolClient.tick(); }

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void sidearm$shootWhileAiming(CallbackInfo ci) { PistolClient.handleAttackClicks(); }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void sidearm$shoot(CallbackInfoReturnable<Boolean> cir) {
        if (PistolClient.equipped()) {
            PistolClient.fire();
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void sidearm$preventMining(boolean held, CallbackInfo ci) {
        if (PistolClient.equipped()) ci.cancel();
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void sidearm$aim(CallbackInfo ci) {
        if (PistolClient.equipped()) {
            PistolClient.startAim();
            ci.cancel();
        }
    }
}
