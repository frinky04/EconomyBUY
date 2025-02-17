package frinky.economybuy.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;


import java.util.List;

public class WadOfCash extends Item{
    public WadOfCash(Item.Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        int count = stack.getCount();
        int money = count * 4;

        tooltip.add(Text.of("$" + money));
    }
}
