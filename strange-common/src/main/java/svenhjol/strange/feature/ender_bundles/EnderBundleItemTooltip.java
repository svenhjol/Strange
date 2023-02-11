package svenhjol.strange.feature.ender_bundles;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm_core.iface.ITooltipGrid;

public class EnderBundleItemTooltip extends BundleTooltip implements ITooltipGrid {
    public EnderBundleItemTooltip(NonNullList<ItemStack> items) {
        super(items, 0);
    }

    @Override
    public int gridSizeX() {
        return 9;
    }

    @Override
    public int gridSizeY() {
        return 3;
    }
}