package sidearm.mixin;

import sidearm.NightVisionGoggles;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GogglesPostEffectMixin {
    @Inject(method = "update", at = @At("TAIL"))
    private void sidearm$greenVision(DeltaTracker deltaTracker, CallbackInfo ci) {
        // Minecraft rebuilds this request list each frame, so taking the goggles off clears the filter.
        if (NightVisionGoggles.active()) {
            ((GameRenderer)(Object)this).getRequestedPostEffects().add(NightVisionGoggles.GREEN_EFFECT);
        }
    }
}
