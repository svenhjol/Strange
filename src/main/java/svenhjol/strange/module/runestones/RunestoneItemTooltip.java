package svenhjol.strange.module.runestones;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.item.ItemStack;

public class RunestoneItemTooltip extends BundleTooltip {
    public RunestoneItemTooltip(NonNullList<ItemStack> items) {
        super(items, 0);
    }
}
