package sidearm.mixin;

import sidearm.SidearmMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeTab.class)
public class PistolCreativeMixin {
    @Inject(method = "buildContents", at = @At("RETURN"))
    private void sidearm$creative(CreativeModeTab.ItemDisplayParameters parameters, CallbackInfo ci) {
        var tab = (CreativeModeTab)(Object)this;
        if (Identifier.withDefaultNamespace("combat").equals(BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab))) {
            var stack = new ItemStack(SidearmMod.PISTOL);
            tab.getDisplayItems().add(stack);
            tab.getSearchTabDisplayItems().add(stack);
            var goggles = new ItemStack(SidearmMod.GOGGLES);
            tab.getDisplayItems().add(goggles);
            tab.getSearchTabDisplayItems().add(goggles);
        }
    }
}
