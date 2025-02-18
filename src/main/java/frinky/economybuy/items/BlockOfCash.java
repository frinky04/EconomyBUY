package frinky.economybuy.items;

import frinky.economybuy.EB_Cash_Interface;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;

public class BlockOfCash extends BlockItem implements EB_Cash_Interface {

    public BlockOfCash(Block block, Item.Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.of("$" + getValue(stack)));
    }


    @Override
    public int getValue(ItemStack stack) {
        return stack.getCount() * 4 * 4 * 9;
    }
}
