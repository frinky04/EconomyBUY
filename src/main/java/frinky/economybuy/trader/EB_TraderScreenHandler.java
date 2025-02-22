package frinky.economybuy.trader;

import frinky.economybuy.EB_Blocks;
import frinky.economybuy.EB_Cash_Interface;
import frinky.economybuy.EB_Items;
import frinky.economybuy.EB_Util;
import frinky.economybuy.economy.EB_EconomyManager;
import frinky.economybuy.economy.EB_ShopItem;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.*;
import java.util.function.UnaryOperator;

public class EB_TraderScreenHandler extends GenericContainerScreenHandler {

    public enum TraderScreen
    {
        SELECTION,
        BUY,
        SELL,
        BANKING
    }

    private final Inventory inventory;
    private final Inventory playerInventory;

    private TraderScreen currentScreen = TraderScreen.SELECTION;
    private List<EB_ShopItem> tradeItems;
    private EB_ShopItem.Category currentCategory = EB_ShopItem.Category.NONE;
    private Item buyingItem = Items.AIR;
    private int page = 0;
    private boolean sellAll = false;

    private final int BACK_SLOT = 8;
    private final int LEFT_ARROW_SLOT = 45;
    private final int RIGHT_ARROW_SLOT = 53;

    private final float CLICK_COOLDOWN = 0.1f;
    private long previousClickTime = 0;
    private int lastPlayerBalance = 0;

    // money values
    private final Map<Integer, Item> moneyValues = new HashMap<>();

    // a map that contains the item in the trader inventory and the item in the player inventory, so we can remove the correct item
    private final Map<ItemStack, ItemStack> sellPlayerItems = new HashMap<>();


    public EB_TraderScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, inventory, 6);

        this.disableSyncing();

        this.inventory = inventory;
        this.playerInventory = playerInventory;



        moneyValues.put(1, EB_Items.CASH);
        moneyValues.put(EB_Items.WAD_OF_CASH.getCashValue(new ItemStack(EB_Items.WAD_OF_CASH)), EB_Items.WAD_OF_CASH);
        moneyValues.put(EB_Items.STACK_OF_CASH.getCashValue(new ItemStack(EB_Items.STACK_OF_CASH)), EB_Items.STACK_OF_CASH);
        moneyValues.put(((EB_Cash_Interface) EB_Blocks.BLOCK_OF_CASH.asItem()).getCashValue(new ItemStack(EB_Blocks.BLOCK_OF_CASH.asItem())), EB_Blocks.BLOCK_OF_CASH.asItem());

        refreshItems();

        Refresh();
    }

    private void refreshItems() {
        tradeItems = EB_EconomyManager.getInstance().shopItems;
    }

    void Refresh() {

        lastPlayerBalance = EB_Util.GetInventoryBalance(playerInventory);


        refreshItems();



        inventory.clear();

        if(currentScreen != TraderScreen.SELECTION)
        {
            ItemStack back = new ItemStack(Items.BARRIER);
            setItemCustomName(back, "Back");
            inventory.setStack(BACK_SLOT, back);
        }

        switch (currentScreen)
        {
            case SELECTION:
                drawSelection();
                break;
            case BUY:
                drawBuy();
                break;
            case SELL:
                drawSell();
                break;
            case BANKING:
                drawBanking();
                break;
        }

        inventory.markDirty();
    }

    private void drawSelection() {
        ItemStack buy = new ItemStack(Items.DIAMOND);
        setItemCustomName(buy, "BUY");
        inventory.setStack(0, buy);

        ItemStack sell = new ItemStack(Items.EMERALD);
        setItemCustomName(sell, "SELL");
        inventory.setStack(1, sell);

        ItemStack banking = new ItemStack(Items.GOLD_INGOT);
        setItemCustomName(banking, "BANKING");
        inventory.setStack(2, banking);
    }

    private void drawSell() {
        if(sellAll)
        {
            ItemStack stack = new ItemStack(Items.GOLD_INGOT);
            setItemCustomName(stack, "SELL ALL");
            setItemLore(stack, "Click to set to sell one mode");
            inventory.setStack(0, stack);
        }
        else
        {
            ItemStack stack = new ItemStack(Items.GOLD_NUGGET);
            setItemCustomName(stack, "SELL ONE");
            setItemLore(stack, "Click to set to sell all mode");
            inventory.setStack(0, stack);
        }

        List<ItemStack> sellableItems = new ArrayList<>();
        for (int i = 0; i < playerInventory.size(); i++) {
            ItemStack stack = playerInventory.getStack(i);
            if(stack.isEmpty())
            {
                continue;
            }
            if(isTradeItem(stack.getItem()))
            {
                sellableItems.add(stack);
            }
        }

        // draw the items in our inventory that are sellable
        int slotIndex = 9;
        for (ItemStack sellableItem : sellableItems) {
            if (slotIndex == BACK_SLOT) {
                slotIndex++;
            }
            ItemStack stack = sellableItem.copy();
            sellPlayerItems.put(stack, sellableItem);
            int price = getTradePrice(stack.getItem(), false);
            if(sellAll)
            {
                setItemLore(stack, "+ $" + price * stack.getCount());
            }
            else
            {
                setItemLore(stack, "+ $" + price);

            }
            inventory.setStack(slotIndex, stack);
            slotIndex++;
        }
    }

    private void drawBanking() {

    }

    // Draw buy based on the current state
    private void drawBuy() {
        if(buyingItem != Items.AIR)
        {
            drawBuy_Item(buyingItem);
            return;
        }

        if(currentCategory.equals(EB_ShopItem.Category.NONE))
        {
            drawBuy_Categories();
            return;
        }

        drawBuy_Items();
    }

    // Draw the items in the current selected category
    private void drawBuy_Items() {
        int i = 0;
        for (EB_ShopItem item : tradeItems) {
            if(i > 52) // only show 45 items at a time
            {
                break;
            }
            if(i == BACK_SLOT || i == LEFT_ARROW_SLOT || i == RIGHT_ARROW_SLOT)
            {
                i++;
            }

            if(!item.category.equals(currentCategory)) // only list items in the current category
            {
                continue;
            }

            if(item.buyPrice == 0) // don't list items that can't be bought
            {
                continue;
            }

            addBuyItem(item, i);
            i++;
        }
    }

    // Draw the categories for the buy screen
    private void drawBuy_Categories() {
        int i = 0;
        for (EB_ShopItem.Category category : EB_ShopItem.Category.values()) {
            if(category.equals(EB_ShopItem.Category.NONE))
            {
                continue;
            }

            ItemStack stack = new ItemStack(Items.BOOK);
            setItemCustomName(stack, category.name());
            inventory.setStack(i, stack);
            i++;
        }
    }

    // Draw the buy screen when the user has selected an item
    private void drawBuy_Item(Item item) {
        int[] amounts = {1, 2, 4, 8, 16, 32, 64};

        for (int i = 0; i < amounts.length; i++) {
            if (amounts[i] > item.getMaxCount()) {
                break;
            }
            ItemStack stack = new ItemStack(item, amounts[i]);
            int price = getTradePrice(item, true) * amounts[i];

            if(price > lastPlayerBalance)
            {
                continue;
            }

            setItemLore(stack, "$" + price);
            inventory.setStack(i, stack);
        }

    }

    @Override
    protected Slot addSlot(Slot slot) {

        return super.addSlot(new UnmodifiableSlot(slot.inventory, slot.getIndex(), slot.x, slot.y));
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex < 0 || slotIndex >= inventory.size()) {
            super.onSlotClick(slotIndex, button, actionType, player);
            return;
        }

        long currentTime = System.currentTimeMillis();

        if(currentTime - previousClickTime < CLICK_COOLDOWN * 1000)
        {
            return;
        }

        previousClickTime = currentTime;

        if(slotIndex == BACK_SLOT)
        {
            player.playSoundToPlayer(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.PLAYERS, 1, 2);
            onBack();
            return;
        }

        switch (currentScreen)
        {
            case SELECTION:
                switch (slotIndex)
                {
                    case 0:
                        player.playSoundToPlayer(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, SoundCategory.PLAYERS, 1, 2);
                        currentScreen = TraderScreen.BUY;
                        break;
                    case 1:
                        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 1, 1);
                        currentScreen = TraderScreen.SELL;
                        break;
                    case 2:
                        player.playSoundToPlayer(SoundEvents.BLOCK_METAL_PLACE, SoundCategory.PLAYERS, 1, 1);
                        currentScreen = TraderScreen.BANKING;
                        break;
                }
                Refresh();
                break;
            case BUY:
                if(buyingItem == Items.AIR)
                {
                    player.playSoundToPlayer(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, SoundCategory.PLAYERS, 1, 2);
                    if(currentCategory == EB_ShopItem.Category.NONE)
                    {
                        // we're selecting a category
                        currentCategory = EB_ShopItem.Category.values()[slotIndex+1];
                        Refresh();
                    }
                    else
                    {
                        // we're selecting an item
                        ItemStack itemStack = slots.get(slotIndex).getStack();
                        if(isTradeItem(itemStack.getItem()))
                        {
                            buyingItem = itemStack.getItem();
                            Refresh();
                        }

                    }
                }
                else
                {
                    // we're selecting an amount
                    ItemStack itemStack = slots.get(slotIndex).getStack();

                    if(itemStack.getItem() == buyingItem)
                    {
                        attemptBuyItem(buyingItem, itemStack.getCount(), player);
                    }

                }
                break;
            case SELL:
                if(slotIndex == 0)
                {
                    player.playSoundToPlayer(SoundEvents.BLOCK_TRIPWIRE_CLICK_ON, SoundCategory.PLAYERS, 1, 1);

                    sellAll = !sellAll;
                    Refresh();
                    return;
                }
                else
                {
                    if(isTradeItem(inventory.getStack(slotIndex).getItem()))
                    {
                        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 1, 1);
                        ItemStack playerItem = sellPlayerItems.get(inventory.getStack(slotIndex));
                        if(!playerItem.isEmpty())
                        {
                            sellItem(playerItem);
                            Refresh();
                        }
                    }
                }
                break;
            case BANKING:

                break;
        }
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    private void onBack() {
        switch (currentScreen) {
            case SELECTION -> {
                // impossible
            }
            case BUY -> {
                if(buyingItem != Items.AIR)
                {
                    buyingItem = Items.AIR;
                    Refresh();
                    return;
                }

                if(currentCategory != EB_ShopItem.Category.NONE)
                {
                    currentCategory = EB_ShopItem.Category.NONE;
                    Refresh();
                    return;
                }
                currentScreen = TraderScreen.SELECTION;
                resetBuy();

            }
            case SELL -> {
                // and this
                currentScreen = TraderScreen.SELECTION;
                resetSell();
            }
            case BANKING -> {
                // we'll fill this out later
                currentScreen = TraderScreen.SELECTION;
            }
        }

        Refresh();

    }

    private void resetBuy() {
        page = 0;
        buyingItem = Items.AIR;
        currentCategory = EB_ShopItem.Category.NONE;
    }

    private void resetSell() {
        sellAll = false;
    }

    private void addBuyItem(EB_ShopItem item, int slot) {

        ItemStack stack = new ItemStack(item.item);
        int price = getTradePrice(item.item, true);
        setItemLore(stack, "- $" + price);
        inventory.setStack(slot, stack);
    }

    private void setItemLore(ItemStack stack, String text) {
        ComponentType<LoreComponent> loreComponentType = DataComponentTypes.LORE;
        LoreComponent defaultLoreComponent = LoreComponent.DEFAULT;
        UnaryOperator<LoreComponent> applier = lore -> lore.with(Text.of(text));

        stack.apply(loreComponentType, defaultLoreComponent, applier);
    }

    private void setItemCustomName(ItemStack stack, String name)  {
        ComponentType<Text> nameComponentType = DataComponentTypes.CUSTOM_NAME;
        UnaryOperator<Text> applier = text -> Text.of(name);

        stack.apply(nameComponentType, null, applier);

    }

    private boolean isTradeItem(Item item) {
        for (EB_ShopItem shopItem : tradeItems) {
            if(shopItem.item == item)
            {
                return true;
            }
        }
        return false;
    }

    private int getTradePrice(Item item, boolean buy) {
        for (EB_ShopItem shopItem : tradeItems) {
            if(shopItem.item == item)
            {
                return buy ? shopItem.buyPrice : shopItem.sellPrice;
            }
        }
        return 0;
    }

    private List<ItemStack> getCashStacks(int price) {
        List<ItemStack> stacks = new ArrayList<>();

        // Get a sorted list of the cash denominations (largest to smallest)
        List<Integer> denominations = new ArrayList<>(moneyValues.keySet());
        denominations.sort(Collections.reverseOrder());

        for (int value : denominations) {
            if (price <= 0) break;  // Stop if we've reached the target value

            // Determine how many items of this denomination are needed.
            int count = price / value;
            if (count > 0) {
                // Create an ItemStack for this cash item.
                ItemStack stack = new ItemStack(moneyValues.get(value), count);
                stacks.add(stack);
                // Reduce the remaining price.
                price -= value * count;
            }
        }
        return stacks;
    }

    private void takeCashFromPlayer(int price) {
        // Step 1: Tally up available coins by denomination from the player's inventory.
        // 'available' maps coin denomination -> count available.
        Map<Integer, Integer> available = new HashMap<>();
        for (int i = 0; i < playerInventory.size(); i++) {
            ItemStack stack = playerInventory.getStack(i);
            if (stack.isEmpty()) continue;
            for (Map.Entry<Integer, Item> entry : moneyValues.entrySet()) {
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
            Item coinItem = moneyValues.get(coinValue);
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
                ((PlayerInventory) playerInventory).offerOrDrop(changeStack);
            }
        }
    }

    private void sellItem(ItemStack playerItem) {
        int price = getTradePrice(playerItem.getItem(), false);
        if(sellAll)
        {
            price *= playerItem.getCount();
        }

        // use the get cash stacks method to get the cash stacks
        List<ItemStack> cashStacks = getCashStacks(price);

        // add the cash stacks to the player inventory
        for (ItemStack cashStack : cashStacks) {
            PlayerInventory castedInventory = (PlayerInventory) playerInventory;
            castedInventory.offerOrDrop(cashStack);
        }
        playerItem.decrement(sellAll ? playerItem.getCount() : 1);

    }

    private void attemptBuyItem(Item item, int amount, PlayerEntity player){
        int playerInventoryBalance = EB_Util.GetInventoryBalance(playerInventory);
        int price = getTradePrice(item, true) * amount;

        if(price > playerInventoryBalance)
        {
            player.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 1, 1);
            return;
        }

        takeCashFromPlayer(price);
        // give the player the item
        ItemStack stack = new ItemStack(item, amount);
        PlayerInventory castedInventory = (PlayerInventory) playerInventory;
        castedInventory.offerOrDrop(stack);

    }

    // Custom slot that prevents item modification
    private static class UnmodifiableSlot extends Slot {
        public UnmodifiableSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return false;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack insertStack(ItemStack stack) {
            return stack;
        }
    }

}