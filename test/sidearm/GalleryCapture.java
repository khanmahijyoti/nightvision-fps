package sidearm.smokemixin;

import sidearm.*;
import java.nio.file.*;
import java.util.Set;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class GalleryCapture {
    private static volatile int stage;
    private static volatile String failure;
    private static int ticks, height;
    private static String pending;
    private static long capturedAfter;
    private static boolean confirmedMissingContent;
    private static final Path GALLERY = Path.of("E:/MinecraftPistol/release/github/gallery");

    @Inject(method = "tick", at = @At("TAIL"))
    private void sidearmGallery$tick(CallbackInfo ci) {
        var mc = (Minecraft)(Object)this;
        var server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) {
            var screen = mc.gui.screen();
            if (!confirmedMissingContent && screen != null && screen.getClass().getName().equals(
                "net.fabricmc.fabric.impl.client.registry.sync.validate.DetailedBackupConfirmScreen")) {
                for (var child : screen.children()) {
                    if (child instanceof net.minecraft.client.gui.components.Button button
                        && button.getMessage().getString().equals("I Know What I'm Doing!")) {
                        confirmedMissingContent = true;
                        button.onPress(new net.minecraft.client.input.KeyEvent(257, 0, 0));
                        break;
                    }
                }
            }
            return;
        }
        if (mc.gui.screen() != null) mc.gui.setScreen(null);
        try {
            ticks++;
            if (failure != null) throw new AssertionError(failure);
            if (ticks > 900) throw new AssertionError("Gallery timed out at stage " + stage);
            if (pending != null && ticks > 15) {
                try (var files = Files.list(Path.of("E:/MinecraftPistol/test-instance/screenshots"))) {
                    var latest = files.filter(path -> path.getFileName().toString().endsWith(".png"))
                        .filter(path -> path.toFile().lastModified() >= capturedAfter)
                        .max(java.util.Comparator.comparingLong(path -> path.toFile().lastModified())).orElseThrow();
                    Files.createDirectories(GALLERY);
                    Files.copy(latest, GALLERY.resolve(pending + ".png"), StandardCopyOption.REPLACE_EXISTING);
                    pending = null;
                }
            }
            if (stage == 0 && ticks > 40) {
                stage = 100;
                server.execute(() -> {
                    try {
                        var p = server.getPlayerList().getPlayer(mc.player.getUUID());
                        var level = server.overworld();
                        level.getChunk(1024 >> 4, 1024 >> 4);
                        height = Math.max(100, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 1024, 1024) + 2);
                        for (int x = 1019; x <= 1029; x++) for (int z = 1020; z <= 1047; z++) {
                            level.setBlock(new BlockPos(x, height - 1, z), Blocks.STONE_BRICKS.defaultBlockState(), 3);
                            for (int y = height; y < height + 4; y++) level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                        }
                        p.getInventory().clearContent();
                        p.setGameMode(GameType.CREATIVE);
                        p.removeAllEffects();
                        p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                        p.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(SidearmMod.PISTOL));
                        p.teleportTo(level, 1024.5, height, 1022.5, Set.of(), 0, 2, true);
                        for (int z : new int[]{1033, 1041}) {
                            var mob = (Mob)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("husk"))
                                .create(level, EntitySpawnReason.COMMAND);
                            mob.setNoAi(true);
                            mob.setPos(1024.5, height, z + .5);
                            level.addFreshEntity(mob);
                        }
                        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "weather clear");
                        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "time set noon");
                        advance(1);
                    } catch (Throwable error) { failure = "Gallery setup: " + error; }
                });
            } else if (stage == 1 && ticks > 70) {
                if (mc.gui.hud.isHidden()) mc.gui.hud.toggle();
                mc.gui.hud.getChat().clearMessages(true);
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                mc.player.setYRot(0);
                mc.player.setXRot(2);
                advance(2);
            } else if (stage == 2 && ticks > 20) {
                capture(mc, "pistol");
                advance(3);
            } else if (stage == 3 && ticks > 20 && pending == null) {
                mc.options.keyUse.setDown(true);
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                advance(4);
            } else if (stage == 4 && ticks > 25) {
                capture(mc, "aiming");
                advance(5);
            } else if (stage == 5 && ticks > 20 && pending == null) {
                mc.options.keyUse.setDown(false);
                mc.gameMode.releaseUsingItem(mc.player);
                stage = 100;
                server.execute(() -> {
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "time set midnight");
                    advance(6);
                });
            } else if (stage == 6 && ticks > 40) {
                capture(mc, "night-without-goggles");
                advance(7);
            } else if (stage == 7 && ticks > 20 && pending == null) {
                stage = 100;
                server.execute(() -> {
                    server.getPlayerList().getPlayer(mc.player.getUUID()).setItemSlot(EquipmentSlot.HEAD, new ItemStack(SidearmMod.GOGGLES));
                    advance(8);
                });
            } else if (stage == 8 && ticks > 25) {
                if (!NightVisionGoggles.active()) throw new AssertionError("Goggles did not activate");
                capture(mc, "green-night-vision");
                advance(9);
            } else if (stage == 9 && ticks > 20 && pending == null) {
                if (!mc.gui.hud.isHidden()) mc.gui.hud.toggle();
                mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
                advance(10);
            } else if (stage == 10 && ticks > 25) {
                capture(mc, "goggles-equipped");
                advance(11);
            } else if (stage == 11 && ticks > 20 && pending == null) {
                Files.writeString(Path.of("E:/MinecraftPistol/gallery-result.txt"), "PASS: five native gameplay screenshots captured for the release gallery.");
                mc.options.keyUse.setDown(false);
                mc.stop();
            }
        } catch (Throwable error) {
            try { Files.writeString(Path.of("E:/MinecraftPistol/gallery-result.txt"), "FAIL: " + error); }
            catch (Exception ignored) { }
            mc.stop();
        }
    }

    private static void capture(Minecraft mc, String name) {
        capturedAfter = System.currentTimeMillis();
        pending = name;
        mc.gui.hud.getChat().clearMessages(true);
        net.minecraft.client.Screenshot.grab(mc.gameDirectory, name + ".png",
            mc.gameRenderer.mainRenderTarget(), 1, text -> { });
    }
    private static void advance(int next) { ticks = 0; stage = next; }
}
