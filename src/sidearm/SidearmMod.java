package sidearm;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.world.entity.EquipmentSlot;

public final class SidearmMod implements ModInitializer {
    public static final Identifier PISTOL_ID = Identifier.fromNamespaceAndPath("iron_sidearm", "iron_pistol");
    public static final ResourceKey<Item> PISTOL_KEY = ResourceKey.create(Registries.ITEM, PISTOL_ID);
    public static final Item PISTOL = Registry.register(BuiltInRegistries.ITEM, PISTOL_KEY,
        new PistolItem(new Item.Properties().setId(PISTOL_KEY).stacksTo(1).rarity(Rarity.UNCOMMON)
            .component(DataComponents.USE_EFFECTS, new UseEffects(false, false, .65f))));
    public static final Identifier GOGGLES_ID = Identifier.fromNamespaceAndPath("iron_sidearm", "night_vision_goggles");
    public static final ResourceKey<Item> GOGGLES_KEY = ResourceKey.create(Registries.ITEM, GOGGLES_ID);
    public static final Item GOGGLES = Registry.register(BuiltInRegistries.ITEM, GOGGLES_KEY,
        new Item(new Item.Properties().setId(GOGGLES_KEY).stacksTo(1).rarity(Rarity.UNCOMMON)
            .equippable(EquipmentSlot.HEAD)));

    @Override
    public void onInitialize() { }
}
