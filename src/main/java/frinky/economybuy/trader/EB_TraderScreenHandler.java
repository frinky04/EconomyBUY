package frinky.economybuy.trader;

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
import net.minecraft.inventory.SimpleInventory;
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

    private final Inventory playerInventory;

    private TraderScreen currentScreen = TraderScreen.SELECTION;
    private List<EB_ShopItem> tradeItems;
    private EB_ShopItem.Category currentCategory = EB_ShopItem.Category.NONE;
    private Item buyingItem = Items.AIR;
    private int page = 0;
    private int maxPage = 0;
    private boolean sellAll = false;

    private final int BACK_SLOT = 8;
    private final int BACK_PAGE_SLOT = 45;
    private final int NEXT_PAGE_SLOT = 53;

    private final float CLICK_COOLDOWN = 0.1f;
    private long previousClickTime = 0;
    private int lastPlayerBalance = 0;

    // money values

    // a map that contains the item in the trader inventory and the item in the player inventory, so we can remove the correct item
    private final Map<ItemStack, ItemStack> sellPlayerItems = new HashMap<>();


    public EB_TraderScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, new SimpleInventory(6*9), 6);

        this.disableSyncing();
        this.playerInventory = playerInventory;





        refreshItems();

        Refresh();
    }

    private void refreshItems() {
        tradeItems = EB_EconomyManager.get().shopItems;
    }

    void Refresh() {

        lastPlayerBalance = EB_Util.GetInventoryBalance(playerInventory);


        refreshItems();



        getInventory().clear();

        if(currentScreen != TraderScreen.SELECTION)
        {
            ItemStack back = new ItemStack(Items.BARRIER);
            setItemCustomName(back, "Back");
            getInventory().setStack(BACK_SLOT, back);
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

        getInventory().markDirty();
    }

    private void drawSelection() {
        ItemStack buy = new ItemStack(Items.DIAMOND);
        setItemCustomName(buy, "BUY");
        getInventory().setStack(0, buy);

        ItemStack sell = new ItemStack(Items.EMERALD);
        setItemCustomName(sell, "SELL");
        getInventory().setStack(1, sell);

        ItemStack banking = new ItemStack(Items.GOLD_INGOT);
        setItemCustomName(banking, "BANKING");
        getInventory().setStack(2, banking);
    }

    private void drawSell() {
        if(sellAll)
        {
            ItemStack stack = new ItemStack(Items.GOLD_INGOT);
            setItemCustomName(stack, "SELL ALL");
            setItemLore(stack, "Click to set to sell one mode");
            getInventory().setStack(0, stack);
        }
        else
        {
            ItemStack stack = new ItemStack(Items.GOLD_NUGGET);
            setItemCustomName(stack, "SELL ONE");
            setItemLore(stack, "Click to set to sell all mode");
            getInventory().setStack(0, stack);
        }

        List<ItemStack> sellableItems = new ArrayList<>();
        for (int i = 0; i < playerInventory.size(); i++) {
            ItemStack stack = playerInventory.getStack(i);
            if(stack.isEmpty())
            {
                continue;
            }
            if(EB_Util.isTradeItem(stack.getItem()))
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
            int price = EB_Util.getTradePrice(stack.getItem(), false);
            if(sellAll)
            {
                setItemLore(stack, "+ $" + price * stack.getCount());
            }
            else
            {
                setItemLore(stack, "+ $" + price);

            }
            getInventory().setStack(slotIndex, stack);
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
        int start = 9;
        int end = 45;  // Changed from 44 to 45 to include the last slot
        int itemsPerPage = end - start;

        List<EB_ShopItem> itemsToList = new ArrayList<>();
        for (EB_ShopItem item : tradeItems) {
            if (item.category.equals(currentCategory) && item.buyPrice > 0) {
                itemsToList.add(item);
            }
        }

        maxPage = (int) Math.ceil((double) itemsToList.size() / itemsPerPage);
        boolean isMoreThanOnePage = maxPage > 1;

        // Navigation arrows
        if (isMoreThanOnePage && page < maxPage - 1) {
            ItemStack stack = new ItemStack(EB_Items.RIGHT_ARROW);
            setItemCustomName(stack, "Page " + (page + 1) + "/" + maxPage);
            getInventory().setStack(NEXT_PAGE_SLOT, stack);
        }
        if (page > 0) {
            ItemStack stack = new ItemStack(EB_Items.LEFT_ARROW);
            setItemCustomName(stack, "Page " + (page + 1) + "/" + maxPage);
            getInventory().setStack(BACK_PAGE_SLOT, stack);
        }

        // Calculate start and end indices for current page
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, itemsToList.size());

        // Draw items for current page
        int slot = start;
        for (int i = startIndex; i < endIndex && slot < end; i++) {
            addBuyItem(itemsToList.get(i), slot);
            slot++;
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
            getInventory().setStack(i, stack);
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
            int price = EB_Util.getTradePrice(item, true) * amounts[i];

            if(price > lastPlayerBalance)
            {
                continue;
            }

            setItemLore(stack, "$" + price);
            getInventory().setStack(i, stack);
        }

    }

    @Override
    protected Slot addSlot(Slot slot) {

        return super.addSlot(new UnmodifiableSlot(slot.inventory, slot.getIndex(), slot.x, slot.y));
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        super.onSlotClick(slotIndex, button, actionType, player);

        if (slotIndex < 0 || slotIndex >= getInventory().size()) {
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
        }
        else {

            switch (currentScreen) {
                case SELECTION:
                    switch (slotIndex) {
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
                    if (buyingItem == Items.AIR)
                    {
                        if (slotIndex == BACK_PAGE_SLOT && slots.get(slotIndex).getStack().getItem() == EB_Items.LEFT_ARROW) {
                            player.playSoundToPlayer(SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON, SoundCategory.PLAYERS, 1, 2);
                            page--;
                            page = Math.max(page, 0);
                            Refresh();
                            return;
                        }

                        if (slotIndex == NEXT_PAGE_SLOT && slots.get(slotIndex).getStack().getItem() == EB_Items.RIGHT_ARROW) {
                            player.playSoundToPlayer(SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON, SoundCategory.PLAYERS, 1, 2);
                            page++;
                            page = Math.min(page, maxPage - 1);
                            Refresh();
                            return;
                        }

                        // if we didn't click air, play a sound
                        if(slots.get(slotIndex).getStack().getItem() != Items.AIR) {
                            player.playSoundToPlayer(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, SoundCategory.PLAYERS, 1, 2);
                        }
                            if (currentCategory == EB_ShopItem.Category.NONE) {
                            // we're selecting a category
                            if (slotIndex < EB_ShopItem.Category.values().length - 1) { // -1 because we skip NONE
                                currentCategory = EB_ShopItem.Category.values()[slotIndex + 1];
                                Refresh();
                            }
                        } else {
                            // we're selecting an item
                            ItemStack itemStack = slots.get(slotIndex).getStack();
                            if (EB_Util.isTradeItem(itemStack.getItem())) {
                                if (EB_Util.getTradePrice(itemStack.getItem(), true) <= lastPlayerBalance) {
                                    buyingItem = itemStack.getItem();
                                    Refresh();
                                } else {
                                    player.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 1, 1);
                                }
                            }

                        }
                    }
                    else
                    {
                        // we're selecting an amount
                        ItemStack itemStack = slots.get(slotIndex).getStack();

                        if (itemStack.getItem() == buyingItem) {
                            attemptBuyItem(buyingItem, itemStack.getCount(), player);
                        }

                    }
                    break;
                case SELL:
                    if (slotIndex == 0) {
                        player.playSoundToPlayer(SoundEvents.BLOCK_TRIPWIRE_CLICK_ON, SoundCategory.PLAYERS, 1, 1);

                        sellAll = !sellAll;
                        Refresh();
                        return;
                    } else {
                        if (EB_Util.isTradeItem(getInventory().getStack(slotIndex).getItem())) {
                            player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 1, 1);
                            ItemStack playerItem = sellPlayerItems.get(getInventory().getStack(slotIndex));
                            if (!playerItem.isEmpty()) {
                                sellItem(playerItem);
                                Refresh();
                            }
                        }
                    }
                    break;
                case BANKING:

                    break;
            }
        }

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
        int price = EB_Util.getTradePrice(item.item, true);
        setItemLore(stack, "$" + price);
        getInventory().setStack(slot, stack);
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

    private void sellItem(ItemStack playerItem) {
        int price = EB_Util.getTradePrice(playerItem.getItem(), false);
        if(sellAll)
        {
            price *= playerItem.getCount();
        }

        // use the get cash stacks method to get the cash stacks
        List<ItemStack> cashStacks = EB_Util.getCashStacks(price);

        // add the cash stacks to the player inventory
        for (ItemStack cashStack : cashStacks) {
            PlayerInventory castedInventory = (PlayerInventory) playerInventory;
            castedInventory.offerOrDrop(cashStack);
        }
        playerItem.decrement(sellAll ? playerItem.getCount() : 1);

    }

    private void attemptBuyItem(Item item, int amount, PlayerEntity player){
        int playerInventoryBalance = EB_Util.GetInventoryBalance(playerInventory);
        int price = EB_Util.getTradePrice(item, true) * amount;

        if(price > playerInventoryBalance)
        {
            player.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 1, 1);
            return;
        }

        EB_Util.takeCashFromPlayer(price, (PlayerInventory) playerInventory);
        // give the player the item
        ItemStack stack = new ItemStack(item, amount);
        PlayerInventory castedInventory = (PlayerInventory) playerInventory;
        castedInventory.offerOrDrop(stack);

        player.playSoundToPlayer(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.PLAYERS, 1, 1);

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