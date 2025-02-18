package frinky.economybuy.items;

import frinky.economybuy.EB_Cash_Interface;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;


import java.util.List;

public class WadOfCash extends Item implements EB_Cash_Interface {
    public WadOfCash(Item.Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.of("$" + getValue(stack)));
    }

    @Override
    public int getValue(ItemStack stack) {
        return stack.getCount() * 4;
    }
}
