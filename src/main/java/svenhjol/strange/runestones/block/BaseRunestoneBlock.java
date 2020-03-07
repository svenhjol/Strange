package svenhjol.strange.runestones.block;

import svenhjol.meson.MesonModule;
import svenhjol.meson.block.MesonBlock;

public abstract class BaseRunestoneBlock extends MesonBlock {
    private int runeValue;

    public BaseRunestoneBlock(MesonModule module, String name, int runeValue, Properties props) {
        super(module, name + "_" + runeValue, props);
        this.runeValue = runeValue;
    }

    public int getRuneValue() {
        return runeValue;
    }
}
