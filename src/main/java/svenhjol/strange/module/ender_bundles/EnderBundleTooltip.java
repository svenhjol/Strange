package svenhjol.strange.module.ender_bundles;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.client.CharmItemTooltip;
import svenhjol.charm.client.ICustomGridsize;

public class EnderBundleTooltip extends CharmItemTooltip implements ICustomGridsize {
    public EnderBundleTooltip(NonNullList<ItemStack> items) {
        super(items);
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
