package svenhjol.strange.runestones.module;

import net.minecraft.item.DyeColor;
import svenhjol.charm.base.CharmCategories;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.runestones.block.MoonstoneBlock;

import java.util.HashMap;
import java.util.Map;

@Module(mod = Strange.MOD_ID, category = CharmCategories.DECORATION)
public class Moonstones extends MesonModule {
    public static Map<Integer, MoonstoneBlock> moonstones = new HashMap<>();

    @Override
    public void init() {
        int i = 0;
        for (DyeColor color : DyeColor.values()) {
            moonstones.put(i++, new MoonstoneBlock(this, color));
        }
    }
}
