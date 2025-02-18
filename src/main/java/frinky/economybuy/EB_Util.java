package frinky.economybuy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class EB_Util {
    public static int GetInventoryBalance(Inventory inventory) {
        int balance = 0;
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).getItem() instanceof EB_Cash_Interface) {
                balance += ((EB_Cash_Interface) inventory.getStack(i).getItem()).getValue(inventory.getStack(i));
            }
        }

        return balance;
    }

    public static int GetNetWorth(PlayerEntity player) {
        int balance = GetInventoryBalance(player.getInventory());
        balance += GetInventoryBalance(player.getEnderChestInventory());
        return balance;

    }
}