package sidearm.mixin;

import sidearm.PistolClient;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class PistolCameraMixin {
    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void sidearm$aimZoom(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (PistolClient.aiming() && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            cir.setReturnValue(cir.getReturnValue() * .70f);
        }
    }
}
