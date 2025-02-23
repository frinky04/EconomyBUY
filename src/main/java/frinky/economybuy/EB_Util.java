package frinky.economybuy;

import frinky.economybuy.economy.EB_EconomyManager;
import frinky.economybuy.economy.EB_ShopItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;

public class EB_Util {
    public static int GetInventoryBalance(Inventory inventory) {
        int balance = 0;
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).getItem() instanceof EB_Cash_Interface) {
                balance += ((EB_Cash_Interface) inventory.getStack(i).getItem()).getCashValue(inventory.getStack(i));
            }
        }

        return balance;
    }

    public static int GetNetWorth(PlayerEntity player) {
        int balance = GetInventoryBalance(player.getInventory());
        balance += GetInventoryBalance(player.getEnderChestInventory());
        return balance;

    }

    public static Item GetItemByName(String name) {
        Identifier id = Identifier.of(name);
        return Registries.ITEM.get(id);
    }

    public static List<ItemStack> getCashStacks(int price) {
        List<ItemStack> stacks = new ArrayList<>();

        // Get a sorted list of the cash denominations (largest to smallest)
        List<Integer> denominations = new ArrayList<>(EB_EconomyManager.get().moneyValues.keySet());
        denominations.sort(Collections.reverseOrder());

        for (int value : denominations) {
            if (price <= 0) break;  // Stop if we've reached the target value

            // Determine how many items of this denomination are needed.
            int count = price / value;
            if (count > 0) {
                // Create an ItemStack for this cash item.
                ItemStack stack = new ItemStack(EB_EconomyManager.get().moneyValues.get(value), count);
                stacks.add(stack);
                // Reduce the remaining price.
                price -= value * count;
            }
        }
        return stacks;
    }

    public static void takeCashFromPlayer(int price, PlayerInventory playerInventory) {
        // Step 1: Tally up available coins by denomination from the player's inventory.
        // 'available' maps coin denomination -> count available.
        Map<Integer, Integer> available = new HashMap<>();
        for (int i = 0; i < playerInventory.size(); i++) {
            ItemStack stack = playerInventory.getStack(i);
            if (stack.isEmpty()) continue;
            for (Map.Entry<Integer, Item> entry : EB_EconomyManager.get().moneyValues.entrySet()) {
                if (stack.getItem() == entry.getValue()) {
                    int coinValue = entry.getKey();
                    available.put(coinValue, available.getOrDefault(coinValue, 0) + stack.getCount());
                }
            }
        }

        // Step 2: Create a sorted list of the coin denominations (ascending order).
        List<Integer> coins = new ArrayList<>(available.keySet());
        Collections.sort(coins);

        // We'll use DFS to find the coin combination that sums to exactly 'price' (or minimally exceeds it).
        // bestCombination will hold the mapping: coin value -> count used.
        final Map<Integer, Integer>[] bestCombination = new Map[]{ null };
        final int[] bestOverpay = new int[]{ Integer.MAX_VALUE };

        // Recursive DFS class.
        class DFS {
            void search(int index, int currentSum, Map<Integer, Integer> combination) {
                // If we've considered all coin types, check if we've reached (or exceeded) the price.
                if (index == coins.size()) {
                    if (currentSum >= price) {
                        int overpay = currentSum - price;
                        // If this combination is better (i.e. lower overpay) than the current best, store it.
                        if (overpay < bestOverpay[0]) {
                            bestOverpay[0] = overpay;
                            bestCombination[0] = new HashMap<>(combination);
                        }
                    }
                    return;
                }
                int coinValue = coins.get(index);
                int maxAvailable = available.get(coinValue);
                // Limit the number of coins to try.
                // We need no more than enough coins to reach the remaining amount (plus one extra to allow overshooting).
                int maxNeeded = (price - currentSum + coinValue - 1) / coinValue;
                maxNeeded = Math.min(maxAvailable, maxNeeded + 1);
                for (int count = 0; count <= maxNeeded; count++) {
                    combination.put(coinValue, count);
                    search(index + 1, currentSum + count * coinValue, combination);
                }
                combination.remove(coinValue);
            }
        }

        DFS dfs = new DFS();
        dfs.search(0, 0, new HashMap<>());

        // If no combination is found (which shouldn't happen if the player has enough cash), exit.
        if (bestCombination[0] == null) {
            return;
        }

        // Calculate the total taken and the overpayment.
        int totalTaken = price + bestOverpay[0];

        // Step 3: Remove the coins used in the best combination from the player's inventory.
        // For each coin type in the best combination, remove that many coins.
        for (Map.Entry<Integer, Integer> entry : bestCombination[0].entrySet()) {
            int coinValue = entry.getKey();
            int countToRemove = entry.getValue();
            if (countToRemove <= 0) continue;
            Item coinItem = EB_EconomyManager.get().moneyValues.get(coinValue);
            for (int i = 0; i < playerInventory.size() && countToRemove > 0; i++) {
                ItemStack stack = playerInventory.getStack(i);
                if (!stack.isEmpty() && stack.getItem() == coinItem) {
                    int stackCount = stack.getCount();
                    if (stackCount <= countToRemove) {
                        countToRemove -= stackCount;
                        playerInventory.setStack(i, ItemStack.EMPTY);
                    } else {
                        stack.decrement(countToRemove);
                        countToRemove = 0;
                    }
                }
            }
        }

        // Step 4: If the combination overpaid, calculate and return change.
        int change = bestOverpay[0];
        if (change > 0) {
            List<ItemStack> changeStacks = getCashStacks(change);
            for (ItemStack changeStack : changeStacks) {
                playerInventory.offerOrDrop(changeStack);
            }
        }
    }

    public static int getTradePrice(Item item, boolean buy) {
        for (EB_ShopItem shopItem : EB_EconomyManager.get().shopItems) {
            if(shopItem.item == item)
            {
                return buy ? shopItem.buyPrice : shopItem.sellPrice;
            }
        }
        return 0;
    }

    public static boolean isTradeItem(Item item) {
        for (EB_ShopItem shopItem : EB_EconomyManager.get().shopItems) {
            if(shopItem.item == item)
            {
                return true;
            }
        }
        return false;
    }

}