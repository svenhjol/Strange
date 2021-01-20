package svenhjol.strange.runicfragments;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.item.CharmItem;

public class RunicFragmentItem extends CharmItem {
    public static final String WORD_TAG = "word";

    public RunicFragmentItem(CharmModule module) {
        super(module, "runic_fragment", new Settings()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxCount(1));
    }

    public static String getWord(ItemStack fragment) {
        if (fragment.getTag() == null || !fragment.getTag().contains(WORD_TAG))
            return "";

        return fragment.getOrCreateTag().getString(WORD_TAG);
    }

    public static void setWord(ItemStack fragment, String word) {
        fragment.getOrCreateTag().putString(WORD_TAG, word);
    }
}
