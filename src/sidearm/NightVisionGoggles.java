package sidearm;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.resources.Identifier;

public final class NightVisionGoggles {
    public static final Identifier GREEN_EFFECT = Identifier.fromNamespaceAndPath("iron_sidearm", "night_vision");
    public static boolean active() {
        var mc = Minecraft.getInstance();
        return mc.getSingleplayerServer() != null && mc.player != null && mc.player.isAlive()
            && !mc.player.isSpectator() && mc.getCameraEntity() == mc.player
            && mc.player.getItemBySlot(EquipmentSlot.HEAD).getItem() == SidearmMod.GOGGLES;
    }
}
