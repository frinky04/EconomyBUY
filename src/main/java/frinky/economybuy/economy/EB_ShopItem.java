package frinky.economybuy.economy;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class EB_ShopItem
{
    public EB_ShopItem(Item item, int buyPrice, int sellPrice, Category category)
    {
        this.item = item;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.category = category;
    }

    public enum Category
    {
        NONE,
        WEAPONS,
        ARMOR,
        TOOLS,
        MATERIALS,
        MISC
    }

    public Item item = Items.AIR;
    public int buyPrice = 0;
    public int sellPrice = 0;
    public Category category = Category.MISC;

}
