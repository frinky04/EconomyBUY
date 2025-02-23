package frinky.economybuy;

import frinky.economybuy.blocks.InterestChestBlock;
import frinky.economybuy.blocks.InterestChestBlockEntity;
import frinky.economybuy.items.BlockOfCash;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
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

    public static Block register(Block block, RegistryKey<Block> blockKey, boolean shouldRegisterItem) {
        // Sometimes, you may not want to register an item for the block.
        // Eg: if it's a technical block like `minecraft:air` or `minecraft:end_gateway`
        if (shouldRegisterItem) {
            // Items need to be registered with a different type of registry key, but the ID
            // can be the same.
            RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, blockKey.getValue());

            BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey));
            Registry.register(Registries.ITEM, itemKey, blockItem);
        }

        return Registry.register(Registries.BLOCK, blockKey, block);
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



    public static final RegistryKey<Block> INTEREST_CHEST_KEY = RegistryKey.of(
            RegistryKeys.BLOCK,
            Identifier.of(EconomyBUY.MOD_ID, "interest_chest")
    );



    public static final Block INTEREST_CHEST = register(
            new InterestChestBlock(
                    AbstractBlock.Settings.create()
                            .registryKey(INTEREST_CHEST_KEY)
                            .sounds(BlockSoundGroup.METAL)
                            .strength(2.5f)
            ), INTEREST_CHEST_KEY, true
    );


    public static BlockEntityType<InterestChestBlockEntity> INTEREST_CHEST_ENTITY_TYPE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(EconomyBUY.MOD_ID, "interest_chest"),
            FabricBlockEntityTypeBuilder.create(InterestChestBlockEntity::new, INTEREST_CHEST).build(null)
    );

}
