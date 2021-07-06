package svenhjol.strange.module.runestones;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.world.item.CreativeModeTab;
import svenhjol.charm.item.CharmItem;
import svenhjol.charm.loader.CharmModule;

public class RunePlateItem extends CharmItem {
    private final int runeValue;

    public RunePlateItem(CharmModule module, int runeValue) {
        super(module, "rune_plate_" + runeValue, new FabricItemSettings()
            .tab(CreativeModeTab.TAB_MISC)
            .stacksTo(64));

        this.runeValue = runeValue;
    }

    public int getRuneValue() {
        return runeValue;
    }
}
