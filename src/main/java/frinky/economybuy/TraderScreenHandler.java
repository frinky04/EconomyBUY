package frinky.economybuy;

import frinky.economybuy.economy.ShopItem;
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
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class TraderScreenHandler extends GenericContainerScreenHandler {

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
    private final List<ShopItem> tradeItems = new ArrayList<>();
    private ShopItem.Category currentCategory = ShopItem.Category.NONE;
    private Item buyingItem = Items.AIR;
    private int page = 0;
    private boolean sellAll = false;

    private final int BACK_SLOT = 8;
    private final int LEFT_ARROW_SLOT = 45;
    private final int RIGHT_ARROW_SLOT = 53;


    public TraderScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, inventory, 6);

        this.disableSyncing();

        this.inventory = inventory;
        this.playerInventory = playerInventory;

        tradeItems.add(new ShopItem(Items.DIAMOND, 100, ShopItem.Category.MATERIALS));
        tradeItems.add(new ShopItem(Items.EMERALD, 50, ShopItem.Category.MATERIALS));
        tradeItems.add(new ShopItem(Items.IRON_INGOT, 10, ShopItem.Category.MATERIALS));
        tradeItems.add(new ShopItem(Items.GOLD_INGOT, 20, ShopItem.Category.MATERIALS));
        tradeItems.add(new ShopItem(Items.COAL, 5, ShopItem.Category.MATERIALS));

        tradeItems.add(new ShopItem(Items.DIAMOND_SWORD, 100, ShopItem.Category.WEAPONS));
        tradeItems.add(new ShopItem(Items.DIAMOND_AXE, 100, ShopItem.Category.WEAPONS));
        tradeItems.add(new ShopItem(Items.DIAMOND_PICKAXE, 100, ShopItem.Category.WEAPONS));


        tradeItems.add(new ShopItem(Items.DIAMOND_HELMET, 100, ShopItem.Category.ARMOR));
        tradeItems.add(new ShopItem(Items.DIAMOND_CHESTPLATE, 100, ShopItem.Category.ARMOR));
        tradeItems.add(new ShopItem(Items.DIAMOND_LEGGINGS, 100, ShopItem.Category.ARMOR));
        tradeItems.add(new ShopItem(Items.DIAMOND_BOOTS, 100, ShopItem.Category.ARMOR));

        tradeItems.add(new ShopItem(Items.DIAMOND_AXE, 100, ShopItem.Category.TOOLS));
        tradeItems.add(new ShopItem(Items.DIAMOND_PICKAXE, 100, ShopItem.Category.TOOLS));
        tradeItems.add(new ShopItem(Items.DIAMOND_SHOVEL, 100, ShopItem.Category.TOOLS));



        Refresh();
    }




    void Refresh()
    {
        inventory.clear();

        if(currentScreen != TraderScreen.SELECTION)
        {
            ItemStack back = new ItemStack(Items.BARRIER);
            setCustomName(back, "Back");
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

    private void drawSelection()
    {
        ItemStack buy = new ItemStack(Items.DIAMOND);
        setCustomName(buy, "BUY");
        inventory.setStack(0, buy);

        ItemStack sell = new ItemStack(Items.EMERALD);
        setCustomName(sell, "SELL");
        inventory.setStack(1, sell);

        ItemStack banking = new ItemStack(Items.GOLD_INGOT);
        setCustomName(banking, "BANKING");
        inventory.setStack(2, banking);
    }

    private void drawSell()
    {
        if(sellAll)
        {
            ItemStack stack = new ItemStack(Items.GOLD_INGOT);
            setCustomName(stack, "SELL ALL");
            setLore(stack, "Click to set to sell one mode");
            inventory.setStack(0, stack);
        }
        else
        {
            ItemStack stack = new ItemStack(Items.GOLD_NUGGET);
            setCustomName(stack, "SELL ONE");
            setLore(stack, "Click to set to sell all mode");
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
            if(sellAll)
            {
                setLore(stack, "$" + getTradePrice(stack.getItem()) * stack.getCount());
            }
            else
            {
                setLore(stack, "$" + getTradePrice(stack.getItem()));

            }
            inventory.setStack(slotIndex, stack);
            slotIndex++;
        }
    }

    private void drawBanking()
    {

    }

    // Draw buy based on the current state
    private void drawBuy() {
        if(buyingItem != Items.AIR)
        {
            drawBuy_Item(buyingItem);
            return;
        }

        if(currentCategory.equals(ShopItem.Category.NONE))
        {
            drawBuy_Categories();
            return;
        }

        drawBuy_Items();
    }

    // Draw the items in the current selected category
    private void drawBuy_Items() {
        int i = 0;
        for (ShopItem item : tradeItems) {
            if(!item.category.equals(currentCategory))
            {
                continue;
            }
            addTradeItem(item, i);
            i++;
        }
    }

    // Draw the categories for the buy screen
    private void drawBuy_Categories() {
        int i = 0;
        for (ShopItem.Category category : ShopItem.Category.values()) {
            if(category.equals(ShopItem.Category.NONE))
            {
                continue;
            }

            ItemStack stack = new ItemStack(Items.BOOK);
            setCustomName(stack, category.name());
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
            setLore(stack, "$" + getTradePrice(item) * amounts[i]);
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

        System.out.println("Slot index: " + slotIndex);

        if(slotIndex == BACK_SLOT)
        {
            onBack();
            return;
        }

        switch (currentScreen)
        {
            case SELECTION:
                switch (slotIndex)
                {
                    case 0:
                        currentScreen = TraderScreen.BUY;
                        break;
                    case 1:
                        currentScreen = TraderScreen.SELL;
                        break;
                    case 2:
                        currentScreen = TraderScreen.BANKING;
                        break;
                }
                Refresh();
                break;
            case BUY:
                if(buyingItem == Items.AIR)
                {
                    if(currentCategory == ShopItem.Category.NONE)
                    {
                        // we're selecting a category
                        currentCategory = ShopItem.Category.values()[slotIndex+1];
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
                }
                break;
            case SELL:
                if(slotIndex == 0)
                {
                    sellAll = !sellAll;
                    Refresh();
                    return;
                }
                else
                {
                    if(isTradeItem(inventory.getStack(slotIndex).getItem()))
                    {
                        // just remove the item from the player inventory
                        
                        Refresh();

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

                if(currentCategory != ShopItem.Category.NONE)
                {
                    currentCategory = ShopItem.Category.NONE;
                    Refresh();
                    return;
                }
                currentScreen = TraderScreen.SELECTION;
                ResetBuy();

            }
            case SELL -> {
                // and this
                currentScreen = TraderScreen.SELECTION;
                ResetSell();
            }
            case BANKING -> {
                // we'll fill this out later
                currentScreen = TraderScreen.SELECTION;
            }
        }

        Refresh();

    }

    private void ResetBuy() {
        page = 0;
        buyingItem = Items.AIR;
        currentCategory = ShopItem.Category.NONE;
    }

    private void ResetSell() {
        sellAll = false;
    }

    private void addTradeItem(ShopItem item, int slot) {
        ItemStack stack = new ItemStack(item.item);
        setLore(stack, "$" + item.price);
        inventory.setStack(slot, stack);
    }

    private void setLore(ItemStack stack, String text) {
        ComponentType<LoreComponent> loreComponentType = DataComponentTypes.LORE;
        LoreComponent defaultLoreComponent = LoreComponent.DEFAULT;
        UnaryOperator<LoreComponent> applier = lore -> lore.with(Text.of(text));

        stack.apply(loreComponentType, defaultLoreComponent, applier);
    }

    private void setCustomName(ItemStack stack, String name)  {
        ComponentType<Text> nameComponentType = DataComponentTypes.CUSTOM_NAME;
        UnaryOperator<Text> applier = text -> Text.of(name);

        stack.apply(nameComponentType, null, applier);

    }

    private boolean isTradeItem(Item item) {
        for (ShopItem shopItem : tradeItems) {
            if(shopItem.item == item)
            {
                return true;
            }
        }
        return false;
    }

    private int getTradePrice(Item item) {
        for (ShopItem shopItem : tradeItems) {
            if(shopItem.item == item)
            {
                return shopItem.price;
            }
        }
        return 0;
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