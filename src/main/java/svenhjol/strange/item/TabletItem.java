package svenhjol.strange.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.item.CharmItem;

import javax.annotation.Nullable;

public abstract class TabletItem extends CharmItem {
    public static final String POS_TAG = "pos";
    public static final String DIMENSION_TAG = "dimension";
    public static final String EXACT_TAG = "exact";

    public TabletItem(CharmModule module, String name, Item.Settings settings) {
        super(module, name, settings
            .maxDamage(10));
    }

    @Nullable
    public static Identifier getDimension(ItemStack tablet) {
        if (!tablet.getOrCreateTag().contains(DIMENSION_TAG))
            return null;

        return new Identifier(tablet.getOrCreateTag().getString(DIMENSION_TAG));
    }

    @Nullable
    public static BlockPos getPos(ItemStack tablet) {
        if (!tablet.getOrCreateTag().contains(POS_TAG))
            return null;

        return BlockPos.fromLong(tablet.getOrCreateTag().getLong(POS_TAG));
    }

    public static boolean getExact(ItemStack tablet) {
        if (!tablet.getOrCreateTag().contains(EXACT_TAG))
            return false;

        return tablet.getOrCreateTag().getBoolean(EXACT_TAG);
    }

    public static void setDimension(ItemStack tablet, Identifier dimension) {
        tablet.getOrCreateTag().putString(DIMENSION_TAG, dimension.toString());
    }

    public static void setPos(ItemStack tablet, BlockPos pos) {
        tablet.getOrCreateTag().putLong(POS_TAG, pos.asLong());
    }

    public static void setExact(ItemStack tablet, boolean exact) {
        tablet.getOrCreateTag().putBoolean(EXACT_TAG, exact);
    }
}
