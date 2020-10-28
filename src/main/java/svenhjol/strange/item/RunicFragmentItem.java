package svenhjol.strange.item;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.item.CharmItem;

public class RunicFragmentItem extends CharmItem {
    public static final String RUNE_TAG = "rune";

    public RunicFragmentItem(CharmModule module) {
        super(module, "runic_fragment", new Settings()
            .group(ItemGroup.MISC)
            .rarity(Rarity.RARE)
            .maxCount(1));
    }

    public static int getRune(ItemStack fragment) {
        if (!fragment.getOrCreateTag().contains(RUNE_TAG))
            return -1;

        return fragment.getOrCreateTag().getInt(RUNE_TAG);
    }

    public static void setRune(ItemStack fragment, int rune) {
        fragment.getOrCreateTag().putInt(RUNE_TAG, rune);
    }
}
