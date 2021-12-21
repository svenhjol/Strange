package svenhjol.strange.module.ruins;

import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;

@CommonModule(mod = Strange.MOD_ID)
public class Ruins extends CharmModule {
    public static boolean stoneRuins = true;

    public static int stoneRuinSize = 5;

    public static List<String> stoneRuinBiomeCategories = List.of(
        "plains", "desert", "mountains", "savanna", "forest", "icy", "mesa"
    );

    public static List<String> stoneRuinDimensionBlacklist = new ArrayList<>();

    public static final List<IRuinType> TYPES = new ArrayList<>();

    @Override
    public void register() {
        if (stoneRuins) {
            TYPES.add(new StoneRuins());
        }

        TYPES.forEach(IRuinType::register);
    }

    @Override
    public void runWhenEnabled() {
        TYPES.forEach(IRuinType::runWhenEnabled);
    }
}
