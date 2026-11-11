package sidearm;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.LevelResource;

/** Separate per-world records keep the starter grant independent of other mods and older saves. */
public final class PistolLoadout {
    private static final Map<MinecraftServer, State> STATES = new WeakHashMap<>();
    private static final class State {
        final Properties issued = new Properties();
        final Set<String> warned = new HashSet<>();
        boolean failed;
    }

    private static Path file(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("iron-sidearm-loadout.properties");
    }

    private static State state(MinecraftServer server) {
        return STATES.computeIfAbsent(server, instance -> {
            var state = new State();
            if (Files.exists(file(instance))) {
                try (var reader = Files.newBufferedReader(file(instance))) { state.issued.load(reader); }
                catch (IOException error) {
                    state.failed = true;
                    System.getLogger("IronSidearm").log(System.Logger.Level.ERROR, "Cannot read pistol grants", error);
                }
            }
            return state;
        });
    }

    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) return;
        var state = state(server);
        if (state.failed) return;
        for (var player : server.getPlayerList().getPlayers()) {
            if (player.isAlive()) issue(player, state);
        }
    }

    public static void afterDeathRespawn(ServerPlayer player) {
        var state = state(player.level().getServer());
        if (state.failed) return;
        String id = player.getUUID().toString();
        if (!hasItem(player, SidearmMod.PISTOL)) {
            state.issued.remove(id);
            state.warned.remove(id);
        }
        if (!hasItem(player, SidearmMod.GOGGLES)) {
            state.issued.remove(id + ".goggles");
            state.warned.remove(id + ".goggles");
        }
        save(player.level().getServer(), state);
        issue(player, state);
    }

    private static boolean hasItem(ServerPlayer player, Item item) {
        return player.getInventory().contains(stack -> stack.getItem() == item)
            || player.getItemBySlot(EquipmentSlot.HEAD).getItem() == item
            || player.getOffhandItem().getItem() == item;
    }

    private static void issue(ServerPlayer player, State state) {
        String id = player.getUUID().toString();
        issueItem(player, state, SidearmMod.PISTOL, id, "your pistol",
            "Pistol received. Left-click to shoot, hold right-click to aim.");
        issueItem(player, state, SidearmMod.GOGGLES, id + ".goggles", "your night-vision goggles",
            "Night-vision goggles received. Right-click to equip, or place them in your helmet slot.");
    }

    private static void issueItem(ServerPlayer player, State state, Item item, String id, String label, String message) {
        if (state.issued.containsKey(id)) return;
        if (!hasItem(player, item)) {
            player.getInventory().add(new ItemStack(item));
            if (!hasItem(player, item)) {
                if (state.warned.add(id)) player.sendSystemMessage(Component.literal(
                    "[Iron Sidearm] Free an inventory slot to receive " + label + "."));
                return;
            }
            player.sendSystemMessage(Component.literal("[Iron Sidearm] " + message));
        }
        state.issued.setProperty(id, BuiltInRegistries.ITEM.getKey(item).toString());
        state.warned.remove(id);
        save(player.level().getServer(), state);
    }

    private static void save(MinecraftServer server, State state) {
        Path destination = file(server);
        Path temporary = destination.resolveSibling(destination.getFileName() + ".tmp");
        try {
            try (var writer = Files.newBufferedWriter(temporary)) { state.issued.store(writer, "Iron Sidearm starter grants"); }
            try { Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException error) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException error) {
            System.getLogger("IronSidearm").log(System.Logger.Level.ERROR, "Cannot save pistol grants", error);
        }
    }
}
