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

    public TabletItem(CharmModule module, String name, Item.Settings settings) {
        super(module, name, settings
            .maxDamage(10));
    }

    @Nullable
    public static Identifier getDimension(ItemStack tablet) {
        String string = tablet.getOrCreateTag().getString(DIMENSION_TAG);
        return string == null ? null : new Identifier(string);
    }

    @Nullable
    public static BlockPos getPos(ItemStack tablet) {
        long tag = tablet.getOrCreateTag().getLong(POS_TAG);
        if (tag == 0)
            return null;

        return BlockPos.fromLong(tag);
    }

    public static void setDimension(ItemStack tablet, Identifier dimension) {
        tablet.getOrCreateTag().putString(DIMENSION_TAG, dimension.toString());
    }

    public static void setPos(ItemStack tablet, BlockPos pos) {
        tablet.getOrCreateTag().putLong(POS_TAG, pos.asLong());
    }
}
