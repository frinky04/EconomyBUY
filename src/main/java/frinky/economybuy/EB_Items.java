package frinky.economybuy;

import frinky.economybuy.items.Cash;
import frinky.economybuy.items.StackOfCash;
import frinky.economybuy.items.WadOfCash;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.function.Function;

public class EB_Items {
    public static Item register(String path, Function<Item.Settings, Item> factory, Item.Settings settings)
    {
        final RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(EconomyBUY.MOD_ID, path));
        return Items.register(registryKey, factory, settings);
    }

    public static void initialize() {

        // Add to group
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(EB_Items.CASH));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(EB_Items.WAD_OF_CASH));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(EB_Items.STACK_OF_CASH));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(EB_Items.RIGHT_ARROW));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(EB_Items.LEFT_ARROW));

        FuelRegistryEvents.BUILD.register((builder, context) -> {
            builder.add(CASH, 50);
            builder.add(WAD_OF_CASH, 100);
            builder.add(STACK_OF_CASH, 200);
        });

    }

    // Item
    // Single Dollar
    public static final Cash CASH = (Cash) register("cash", Cash::new, new Item.Settings().rarity(Rarity.UNCOMMON));

    // 9 Dollars
    public static final WadOfCash WAD_OF_CASH = (WadOfCash) register("wad_of_cash", WadOfCash::new, new Item.Settings().rarity(Rarity.RARE));

    // 81 Dollars
    public static final StackOfCash STACK_OF_CASH = (StackOfCash) register("stack_of_cash", StackOfCash::new, new Item.Settings().rarity(Rarity.EPIC));

    // Items for UI
    public static final Item RIGHT_ARROW = register("right_arrow", Item::new, new Item.Settings().rarity(Rarity.UNCOMMON));
    public static final Item LEFT_ARROW = register("left_arrow", Item::new, new Item.Settings().rarity(Rarity.UNCOMMON));
}