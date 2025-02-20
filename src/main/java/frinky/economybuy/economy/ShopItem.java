package frinky.economybuy.economy;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class ShopItem
{
    public ShopItem(Item item, int price, Category category)
    {
        this.item = item;
        this.price = price;
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
    public int price = 0;
    public Category category = Category.MISC;

}
