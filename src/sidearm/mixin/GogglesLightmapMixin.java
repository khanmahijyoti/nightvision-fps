package sidearm.mixin;

import sidearm.NightVisionGoggles;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public class GogglesLightmapMixin {
    @Inject(method = "extract", at = @At("RETURN"))
    private void sidearm$nightVision(LightmapRenderState state, float partialTick, CallbackInfo ci) {
        if (state.needsUpdate && NightVisionGoggles.active()) state.nightVisionEffectIntensity = 1;
    }
}
