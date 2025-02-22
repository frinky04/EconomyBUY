package frinky.economybuy.items;

import frinky.economybuy.EB_Cash_Interface;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;

public class StackOfCash extends Item implements EB_Cash_Interface {
    public StackOfCash(Item.Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.of("$" + getCashValue(stack)));
    }
    @Override
    public int getCashValue(ItemStack stack) {
        return stack.getCount() * 4 * 4;

    }
}
