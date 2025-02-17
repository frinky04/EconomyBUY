package frinky.economybuy.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;

public class StackOfCash extends Item {
    public StackOfCash(Item.Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        int money = stack.getCount() * 4 * 4;
        tooltip.add(Text.of("$" + money));
    }
}
