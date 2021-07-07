package svenhjol.strange.module.runic_tomes;

import net.minecraft.world.item.Item;
import svenhjol.charm.item.CharmItem;
import svenhjol.charm.loader.CharmModule;

public class RunicTomeItem extends CharmItem {
    public static final String TAG_TYPE = "type";
    public static final String TAG_DIMENSION = "dimension";
    public static final String TAG_AUTHOR = "author";
    public static final String TAG_CONTENTS = "contents";
    public static final String TAG_SEED = "seed";
    public static final String TAG_RARITY = "rarity";
    public static final String TAG_REQUIRED_ITEMS = "required_items";
    public static final String TAG_REQUIRED_XP = "required_xp";
    public static final String TAG_DESCRIPTION = "description";
    public static final String TAG_DEGRADATION = "degradation";

    public RunicTomeItem(CharmModule module) {
        super(module, "runic_tome", (new Item.Properties()).stacksTo(1));
    }
}
