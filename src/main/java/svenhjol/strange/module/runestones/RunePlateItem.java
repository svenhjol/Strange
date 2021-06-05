package svenhjol.strange.module.runestones;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.ItemGroup;
import svenhjol.charm.item.CharmItem;
import svenhjol.charm.module.CharmModule;

public class RunePlateItem extends CharmItem {
    private final int runeValue;

    public RunePlateItem(CharmModule module, int runeValue) {
        super(module, "rune_plate_" + runeValue, new FabricItemSettings()
            .group(ItemGroup.MISC)
            .maxCount(64));

        this.runeValue = runeValue;
    }

    public int getRuneValue() {
        return runeValue;
    }
}
