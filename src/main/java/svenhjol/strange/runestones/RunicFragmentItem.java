package svenhjol.strange.runestones;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.ItemGroup;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.item.CharmItem;

public class RunicFragmentItem extends CharmItem {
    private final int runeValue;

    public RunicFragmentItem(CharmModule module, int runeValue) {
        super(module, "runic_fragment_" + runeValue, new FabricItemSettings()
            .group(ItemGroup.MISC)
            .maxCount(64));

        this.runeValue = runeValue;
    }

    public int getRuneValue() {
        return runeValue;
    }
}
