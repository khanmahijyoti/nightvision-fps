package sidearm.smokemixin;

import sidearm.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class PistolSmokeTest {
    private static volatile int stage;
    private static volatile String failure;
    private static volatile Mob target;
    private static int ticks;
    private static float normalFov;
    private static net.minecraft.client.player.LocalPlayer previous;
    private static int loadingTicks;
    private static boolean confirmedMissingContent;

    @Inject(method = "tick", at = @At("TAIL"))
    private void sidearmTest$tick(CallbackInfo ci) {
        var mc = (Minecraft)(Object)this;
        var server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) {
            var screen = mc.gui.screen();
            // This harness operates only on its disposable copy of the user's world.
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
            if (++loadingTicks == 200) {
                System.out.println("Sidearm loading screen: " + (mc.gui.screen() == null ? "none" : mc.gui.screen().getClass().getName()));
                net.minecraft.client.Screenshot.grab(mc, true);
            }
            return;
        }
        try {
            ticks++;
            if (failure != null) throw new AssertionError(failure);
            if (ticks > 900) throw new AssertionError("Timeout at stage " + stage);
            if (mc.gui.screen() != null) mc.gui.setScreen(null);
            if (stage == 0 && ticks > 40) {
                if (!mc.player.getInventory().contains(s -> s.getItem() == SidearmMod.PISTOL)) return;
                if (!mc.player.getInventory().contains(s -> s.getItem() == SidearmMod.GOGGLES)
                    && mc.player.getItemBySlot(EquipmentSlot.HEAD).getItem() != SidearmMod.GOGGLES) return;
                require(FabricLoader.getInstance().isModLoaded("ashen_arena") == Boolean.getBoolean("sidearm.test.withArena"), "Unexpected Ashen Arena presence");
                var model = mc.getModelManager().getItemModel(SidearmMod.PISTOL_ID);
                require(model != null && !model.getClass().getName().contains("Missing"), "Missing pistol model");
                var gogglesModel = mc.getModelManager().getItemModel(SidearmMod.GOGGLES_ID);
                require(gogglesModel != null && !gogglesModel.getClass().getName().contains("Missing"), "Missing goggles model");
                CreativeModeTabs.tryRebuildTabContents(mc.level.enabledFeatures(), true, mc.level.registryAccess());
                var combat = BuiltInRegistries.CREATIVE_MODE_TAB.getValue(Identifier.withDefaultNamespace("combat"));
                require(combat.getDisplayItems().stream().anyMatch(s -> s.getItem() == SidearmMod.PISTOL), "Creative entry missing");
                require(CreativeModeTabs.searchTab().getDisplayItems().stream().anyMatch(s -> s.getItem() == SidearmMod.PISTOL), "Search entry missing");
                stage = 100;
                server.execute(() -> {
                    try {
                        var p = player(server, mc);
                        server.overworld().getEntitiesOfClass(Mob.class,
                            new net.minecraft.world.phys.AABB(-10, 190, -10, 10, 210, 80)).forEach(Mob::discard);
                        p.getInventory().clearContent();
                        p.setGameMode(GameType.SURVIVAL);
                        p.setHealth(20);
                        p.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(SidearmMod.PISTOL));
                        for (int x = -8; x <= 8; x++) for (int z = -6; z <= 76; z++) {
                            server.overworld().setBlock(new BlockPos(x, 200, z), Blocks.STONE.defaultBlockState(), 3);
                            for (int y = 201; y <= 204; y++) server.overworld().setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                        }
                        p.teleportTo(server.overworld(), .5, 201, .5, Set.of(), 0, 3, true);
                        advance(1);
                    } catch (Throwable error) { failure = "Setup: " + error; }
                });
            } else if (stage == 1 && ticks > 35 && PistolClient.equipped()) {
                stage = 100;
                server.execute(() -> {
                    try {
                        // Entity loading follows chunk loading. Clear old test targets after arrival settles.
                        server.overworld().getEntitiesOfClass(Mob.class,
                            new net.minecraft.world.phys.AABB(-10, 190, -10, 10, 210, 80)).forEach(Mob::discard);
                        target = cow(server, 12.5);
                        advance(16);
                    } catch (Throwable error) { failure = "Target preparation: " + error; }
                });
            } else if (stage == 16 && ticks > 20) {
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                mc.player.setYRot(0);
                mc.player.setXRot(3);
                normalFov = mc.gameRenderer.mainCamera().getFov();
                net.minecraft.client.Screenshot.grab(mc, true);
                clickAttack(mc);
                advance(2);
            } else if (stage == 2 && ticks > 25) {
                stage = 100;
                server.execute(() -> {
                    try {
                        require(Math.abs(target.getHealth() - 2) < .01, "Left-click/held fire expected one 8-damage shot, health=" + target.getHealth());
                        require(!PistolCombat.fire(player(server, mc), Float.NaN, 0), "Invalid aim accepted");
                        advance(3);
                    } catch (Throwable error) { failure = "Hip fire: " + error; }
                });
            } else if (stage == 3) {
                mc.options.keyAttack.setDown(false);
                mc.options.keyUse.setDown(true);
                KeyMapping.click(mc.options.keyUse.getDefaultKey());
                advance(4);
            } else if (stage == 4 && ticks > 20) {
                require(PistolClient.aiming(), "Right-click did not start aiming");
                require(mc.gameRenderer.mainCamera().getFov() < normalFov * .8f, "ADS did not zoom");
                mc.player.setYRot(0);
                mc.player.setXRot(3);
                net.minecraft.client.Screenshot.grab(mc, true);
                clickAttack(mc);
                advance(5);
            } else if (stage == 5 && ticks > 20) {
                stage = 100;
                server.execute(() -> {
                    try {
                        require(player(server, mc).isUsingItem(), "Aim did not reach server");
                        require(target.isDeadOrDying() || target.isRemoved(), "Left-click while aiming did not hit");
                        advance(6);
                    } catch (Throwable error) { failure = "ADS fire: " + error; }
                });
            } else if (stage == 6) {
                mc.options.keyUse.setDown(false);
                mc.options.keyAttack.setDown(false);
                advance(7);
            } else if (stage == 7 && ticks > 15) {
                require(!PistolClient.aiming() && !mc.player.isUsingItem(), "Aim stuck after release");
                require(Math.abs(mc.gameRenderer.mainCamera().getFov() - normalFov) < 2, "FOV did not restore");
                stage = 100;
                server.execute(() -> {
                    try {
                        var p = player(server, mc);
                        target = cow(server, 12.5);
                        for (int x = -1; x <= 1; x++) for (int y = 201; y <= 204; y++) {
                            server.overworld().setBlock(new BlockPos(x, y, 6), Blocks.STONE.defaultBlockState(), 3);
                        }
                        require(PistolCombat.fire(p, 0, 3), "Wall shot rejected");
                        require(target.getHealth() == 10, "Shot passed through wall");
                        require(server.overworld().getBlockState(new BlockPos(0, 202, 6)).is(Blocks.STONE), "Shot destroyed wall");
                        require(!PistolCombat.fire(p, 0, 3), "Server cooldown bypassed");
                        advance(8);
                    } catch (Throwable error) { failure = "Wall/cooldown: " + error; }
                });
            } else if (stage == 8 && ticks > 15) {
                stage = 100;
                server.execute(() -> {
                    try {
                        target.discard();
                        var p = player(server, mc);
                        for (int x = -1; x <= 1; x++) for (int y = 201; y <= 204; y++) {
                            server.overworld().setBlock(new BlockPos(x, y, 6), Blocks.AIR.defaultBlockState(), 3);
                        }
                        target = cow(server, 70.5);
                        require(PistolCombat.fire(p, 0, 0), "Range shot rejected");
                        require(target.getHealth() == 10, "Shot exceeded maximum range");
                        target.discard();
                        target = cow(server, 12.5);
                        advance(9);
                    } catch (Throwable error) { failure = "Range: " + error; }
                });
            } else if (stage == 9 && ticks > 15) {
                stage = 100;
                server.execute(() -> {
                    try {
                        var p = player(server, mc);
                        var near = cow(server, 5.5);
                        require(PistolCombat.fire(p, 0, 3), "Nearest-target shot rejected");
                        require(near.getHealth() == 2 && target.getHealth() == 10, "Shot did not stop at nearest target");
                        near.discard();
                        target.discard();
                        p.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STICK));
                        require(!PistolCombat.fire(p, 0, 3), "Shot allowed without equipped pistol");
                        p.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(SidearmMod.PISTOL));
                        p.setGameMode(GameType.SPECTATOR);
                        require(!PistolCombat.fire(p, 0, 3), "Spectator shot accepted");
                        p.setGameMode(GameType.SURVIVAL);
                        server.getGameRules().set(GameRules.KEEP_INVENTORY, false, server);
                        p.setInvulnerableTime(0);
                        p.damageCooldownTime = 0;
                        p.kill(p.level());
                        require(p.isDeadOrDying(), "Actual death failed");
                        advance(10);
                    } catch (Throwable error) { failure = "Nearest target/restrictions/death: " + error; }
                });
            } else if (stage == 10 && mc.player.isDeadOrDying()) {
                previous = mc.player;
                mc.player.respawn();
                advance(11);
            } else if (stage == 11 && mc.player != previous && mc.player.isAlive() && ticks > 25) {
                stage = 100;
                server.execute(() -> {
                    try {
                        var p = player(server, mc);
                        require(count(p) == 1, "Death respawn did not grant exactly one pistol");
                        require(p.getInventory().getNonEquipmentItems().stream().filter(s -> s.is(SidearmMod.GOGGLES)).count() == 1, "Goggles not replaced after death");
                        var named = p.getInventory().getNonEquipmentItems().stream().filter(s -> s.getItem() == SidearmMod.PISTOL).findFirst().orElseThrow();
                        named.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Kept Pistol"));
                        server.getGameRules().set(GameRules.KEEP_INVENTORY, true, server);
                        p.setInvulnerableTime(0);
                        p.damageCooldownTime = 0;
                        p.kill(p.level());
                        require(p.isDeadOrDying(), "Keep Inventory death failed");
                        advance(12);
                    } catch (Throwable error) { failure = "Respawn grant: " + error; }
                });
            } else if (stage == 12 && mc.player.isDeadOrDying()) {
                previous = mc.player;
                mc.player.respawn();
                advance(13);
            } else if (stage == 13 && mc.player != previous && mc.player.isAlive() && ticks > 25) {
                stage = 100;
                server.execute(() -> {
                    try {
                        var p = player(server, mc);
                        require(count(p) == 1, "Keep Inventory duplicated pistol");
                        require(p.getInventory().getNonEquipmentItems().stream().anyMatch(s -> s.getItem() == SidearmMod.PISTOL && s.getHoverName().getString().equals("Kept Pistol")), "Original pistol lost");
                        for (int slot = 0; slot < p.getInventory().getNonEquipmentItems().size(); slot++) {
                            p.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
                        }
                        PistolLoadout.afterDeathRespawn(p);
                        require(count(p) == 0, "Full inventory overwritten");
                        for (var stack : p.getInventory().getNonEquipmentItems()) require(stack.is(Items.STONE) && stack.getCount() == 64, "Inventory contents damaged");
                        p.getInventory().setItem(8, ItemStack.EMPTY);
                        advance(14);
                    } catch (Throwable error) { failure = "Keep Inventory/full inventory: " + error; }
                });
            } else if (stage == 14 && ticks > 45) {
                stage = 100;
                server.execute(() -> {
                    try {
                        var p = player(server, mc);
                        require(count(p) == 1, "Pending pistol not delivered after freeing slot");
                        p.getInventory().clearContent();
                        p.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(SidearmMod.PISTOL));
                        advance(15);
                    } catch (Throwable error) { failure = "Pending grant: " + error; }
                });
            } else if (stage == 15 && ticks > 25) {
                stage = 100;
                server.execute(() -> {
                    try {
                        var p = player(server, mc);
                        require(p.getInventory().contains(s -> s.is(SidearmMod.GOGGLES)), "Pending goggles not delivered");
                        p.removeEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION);
                        p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                        p.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(SidearmMod.GOGGLES));
                        for (int x = -4; x <= 4; x++) for (int z = -4; z <= 4; z++) for (int y = 201; y <= 205; y++) {
                            boolean wall = Math.abs(x) == 4 || Math.abs(z) == 4 || y == 205;
                            server.overworld().setBlock(new BlockPos(x, y, z),
                                (wall ? Blocks.STONE : Blocks.AIR).defaultBlockState(), 3);
                        }
                        p.teleportTo(server.overworld(), .5, 201, .5, Set.of(), 0, 0, true);
                        advance(17);
                    } catch (Throwable error) { failure = "Goggles preparation: " + error; }
                });
            } else if (stage == 17 && ticks > 35) {
                require(!NightVisionGoggles.active() && nightVision(mc) == 0, "Goggles active while only held");
                net.minecraft.client.Screenshot.grab(mc, true);
                require(mc.gameMode.useItem(mc.player, net.minecraft.world.InteractionHand.MAIN_HAND).consumesAction(), "Right-click failed to equip goggles");
                advance(18);
            } else if (stage == 18 && ticks > 20) {
                require(NightVisionGoggles.active() && nightVision(mc) == 1, "Equipped goggles did not enable night lighting");
                require(mc.gameRenderer.getRequestedPostEffects().contains(NightVisionGoggles.GREEN_EFFECT), "Green filter was not requested");
                require(mc.gameRenderer.getAppliedPostEffects().contains(NightVisionGoggles.GREEN_EFFECT), "Green filter did not load and render");
                require(!mc.player.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION), "Goggles changed potion state");
                if (!mc.gui.hud.isHidden()) mc.gui.hud.toggle();
                net.minecraft.client.Screenshot.grab(mc, true);
                mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
                advance(19);
            } else if (stage == 19 && ticks > 20) {
                net.minecraft.client.Screenshot.grab(mc, true);
                if (mc.gui.hud.isHidden()) mc.gui.hud.toggle();
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                stage = 100;
                server.execute(() -> {
                    try {
                        var p = player(server, mc);
                        p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                        p.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(SidearmMod.PISTOL));
                        advance(20);
                    } catch (Throwable error) { failure = "Goggles removal: " + error; }
                });
            } else if (stage == 20 && ticks > 20) {
                require(!NightVisionGoggles.active() && nightVision(mc) == 0, "Night lighting remained after unequipping");
                require(!mc.gameRenderer.getRequestedPostEffects().contains(NightVisionGoggles.GREEN_EFFECT), "Green filter remained after unequipping");
                require(!mc.gameRenderer.getAppliedPostEffects().contains(NightVisionGoggles.GREEN_EFFECT), "Green filter still rendered after unequipping");
                stage = 100;
                server.execute(() -> {
                    try {
                        var p = player(server, mc);
                        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION, 1200));
                        p.setItemSlot(EquipmentSlot.HEAD, new ItemStack(SidearmMod.GOGGLES));
                        advance(21);
                    } catch (Throwable error) { failure = "Potion setup: " + error; }
                });
            } else if (stage == 21 && ticks > 20) {
                require(nightVision(mc) == 1, "Goggles/potion lighting failed");
                stage = 100;
                server.execute(() -> {
                    try {
                        var p = player(server, mc);
                        p.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                        require(p.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION), "Goggles removal deleted potion");
                        advance(22);
                    } catch (Throwable error) { failure = "Potion preservation: " + error; }
                });
            } else if (stage == 22 && ticks > 20) {
                require(!NightVisionGoggles.active() && nightVision(mc) > .99f, "Potion night vision did not survive unequipping");
                stage = 100;
                server.execute(() -> {
                    try {
                        var p = player(server, mc);
                        p.removeEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION);
                        p.setItemSlot(EquipmentSlot.HEAD, new ItemStack(SidearmMod.GOGGLES));
                        p.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(SidearmMod.PISTOL));
                        advance(23);
                    } catch (Throwable error) { failure = "Final equipment: " + error; }
                });
            } else if (stage == 23 && ticks > 20) {
                result("PASS: " + (Boolean.getBoolean("sidearm.test.withArena") ? "coexistence with Ashen Arena" : "standalone with Ashen Arena absent") + "; starter delivery; models and Creative/search entries; real left-click hits deal 8 damage; holding fire stays semi-automatic; right-click starts server/client aim and camera zoom; shooting during aim; release restores FOV; solid-wall obstruction and preserved blocks; server cooldown; 64-block range; nearest target; item/spectator restrictions; real death respawn replacement; Keep Inventory preserves original; full inventory retains contents and pending grant retries.");
                result(Files.readString(Path.of("E:/MinecraftPistol/pistol-test-result.txt")) + " Goggles: upgrade starter grant, death replacement, pending inventory retry, native right-click helmet equip, dark-enclosure lighting, immediate removal, and potion preservation pass; first/third-person screenshots captured.");
                mc.stop();
            }
        } catch (Throwable error) {
            result("FAIL: stage=" + stage + " " + error);
            mc.options.keyUse.setDown(false);
            mc.options.keyAttack.setDown(false);
            mc.stop();
        }
    }

    private static void clickAttack(Minecraft mc) {
        mc.options.keyAttack.setDown(true);
        KeyMapping.click(mc.options.keyAttack.getDefaultKey());
    }

    private static ServerPlayer player(MinecraftServer server, Minecraft mc) {
        return server.getPlayerList().getPlayer(mc.player.getUUID());
    }

    private static Mob cow(MinecraftServer server, double z) {
        var mob = (Mob)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("cow"))
            .create(server.overworld(), EntitySpawnReason.COMMAND);
        mob.setNoAi(true);
        mob.setNoGravity(true);
        mob.setPos(.5, 201, z);
        server.overworld().addFreshEntity(mob);
        return mob;
    }

    private static int count(ServerPlayer player) {
        return player.getInventory().getNonEquipmentItems().stream().filter(s -> s.getItem() == SidearmMod.PISTOL)
            .mapToInt(ItemStack::getCount).sum();
    }

    private static float nightVision(Minecraft mc) {
        var extractor = new net.minecraft.client.renderer.LightmapRenderStateExtractor(mc.gameRenderer, mc);
        var state = new net.minecraft.client.renderer.state.LightmapRenderState();
        extractor.tick();
        extractor.extract(state, 1);
        return state.nightVisionEffectIntensity;
    }

    private static void advance(int next) { ticks = 0; stage = next; }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private static void result(String message) {
        try { Files.writeString(Path.of("E:/MinecraftPistol/pistol-test-result.txt"), message); }
        catch (Exception error) { throw new RuntimeException(error); }
    }
}
