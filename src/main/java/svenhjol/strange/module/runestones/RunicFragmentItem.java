package svenhjol.strange.module.runestones;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.ItemGroup;
import svenhjol.charm.item.CharmItem;
import svenhjol.charm.module.CharmModule;

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
