package svenhjol.strange.runestones.module;

import net.minecraft.item.DyeColor;
import svenhjol.charm.base.CharmCategories;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.runestones.block.MoonstoneBlock;

import java.util.*;

@Module(mod = Strange.MOD_ID, category = CharmCategories.DECORATION)
public class Moonstones extends MesonModule {
    public static List<MoonstoneBlock> moonstones = new ArrayList<>();

    @Override
    public void init() {
        int i = 0;

        List<DyeColor> colors = new ArrayList<>(Arrays.asList(DyeColor.MAGENTA, DyeColor.PURPLE));

        for (DyeColor color : colors) {
            moonstones.add(new MoonstoneBlock(this, color));
        }
    }
}
