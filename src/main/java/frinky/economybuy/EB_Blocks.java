package frinky.economybuy;

import frinky.economybuy.items.BlockOfCash;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class EB_Blocks {
    public static Block register_block_of_cash(Block block) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, BLOCK_OF_CASH_KEY.getValue());

        Item.Settings settings = new Item.Settings().registryKey(itemKey)
                .rarity(Rarity.EPIC);

        BlockItem blockItem = new BlockOfCash(block, settings);
        Registry.register(Registries.ITEM, itemKey, blockItem);


        return Registry.register(Registries.BLOCK, BLOCK_OF_CASH_KEY, block);
    }

    public static void initialize() {
        FlammableBlockRegistry.getDefaultInstance().add(BLOCK_OF_CASH, 100, 1);
    }


    public static final RegistryKey<Block> BLOCK_OF_CASH_KEY = RegistryKey.of(
            RegistryKeys.BLOCK,
            Identifier.of(EconomyBUY.MOD_ID, "block_of_cash")
    );

    public static final Block BLOCK_OF_CASH = register_block_of_cash(
            new Block(AbstractBlock.Settings.create().
                    registryKey(BLOCK_OF_CASH_KEY).
                    sounds(BlockSoundGroup.GRASS).
                    burnable().
                    strength(0.5f))
    );


}
