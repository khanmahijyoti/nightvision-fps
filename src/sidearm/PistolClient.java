package sidearm;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;

public final class PistolClient {
    private static LocalPlayer owner;
    private static int cooldown;

    public static boolean equipped() {
        var mc = Minecraft.getInstance();
        return mc.getSingleplayerServer() != null && mc.player != null && mc.player.isAlive()
            && !mc.player.isSpectator() && mc.player.getMainHandItem().getItem() == SidearmMod.PISTOL;
    }

    public static boolean aiming() {
        var mc = Minecraft.getInstance();
        return equipped() && mc.gui.screen() == null && mc.options.keyUse.isDown()
            && mc.player.isUsingItem() && mc.player.getUseItem().getItem() == SidearmMod.PISTOL;
    }

    public static void tick() {
        var mc = Minecraft.getInstance();
        if (owner != mc.player) {
            owner = mc.player;
            cooldown = 0;
        }
        if (cooldown > 0) cooldown--;
    }

    public static void handleAttackClicks() {
        var mc = Minecraft.getInstance();
        if (!equipped() || mc.gui.screen() != null) return;
        // Vanilla discards attack clicks during held item use. Handle these before its aim/use branch.
        while (mc.options.keyAttack.consumeClick()) fire();
    }

    public static boolean fire() {
        var mc = Minecraft.getInstance();
        if (!equipped() || mc.gui.screen() != null || cooldown > 0 || mc.player.isSleeping()
            || mc.player.isPassenger()) return false;
        if (mc.player.isUsingItem() && mc.player.getUseItem().getItem() != SidearmMod.PISTOL) return false;
        cooldown = PistolCombat.COOLDOWN_TICKS;
        var server = mc.getSingleplayerServer();
        var id = mc.player.getUUID();
        var dimension = mc.player.level().dimension();
        float yaw = mc.player.getYRot();
        float pitch = mc.player.getXRot();
        server.execute(() -> {
            var player = server.getPlayerList().getPlayer(id);
            if (player != null && player.level().dimension().equals(dimension)) PistolCombat.fire(player, yaw, pitch);
        });
        mc.player.setXRot(Math.max(-90, pitch - (aiming() ? .65f : 1.25f)));
        return true;
    }

    public static void startAim() {
        var mc = Minecraft.getInstance();
        if (mc.gameMode != null && equipped() && mc.gui.screen() == null && !mc.player.isUsingItem()) {
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        }
    }
}
