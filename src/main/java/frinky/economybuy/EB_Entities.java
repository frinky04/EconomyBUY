package frinky.economybuy;

import com.google.common.base.Suppliers;
import frinky.economybuy.blocks.InterestChestBlockEntity;
import frinky.economybuy.trader.EB_Trader;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class EB_Entities {


    public static final RegistryKey<EntityType<?>> TRADER_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(EconomyBUY.MOD_ID, "trader"));
    public static final Identifier TRADER_ID = Identifier.of(EconomyBUY.MOD_ID, "trader");
    public static final EntityType<EB_Trader> TRADER_ENTITY = Registry.register(Registries.ENTITY_TYPE, TRADER_ID, EntityType.Builder.create(EB_Trader::new, SpawnGroup.CREATURE)
            .dimensions(0.6f, 1.95f)
            .build(TRADER_KEY));

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(TRADER_ENTITY, EB_Trader.createMobAttributes());
    }


}

