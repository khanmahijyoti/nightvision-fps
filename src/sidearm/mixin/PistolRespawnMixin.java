package sidearm.mixin;

import sidearm.PistolLoadout;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PistolRespawnMixin {
    @Inject(method = "respawn", at = @At("RETURN"))
    private void sidearm$replace(ServerPlayer previous, boolean alive, Entity.RemovalReason reason,
                                 CallbackInfoReturnable<ServerPlayer> cir) {
        if (!alive) PistolLoadout.afterDeathRespawn(cir.getReturnValue());
    }
}
