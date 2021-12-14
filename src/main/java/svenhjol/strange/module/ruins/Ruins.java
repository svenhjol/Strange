package svenhjol.strange.module.ruins;

import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CommonModule(mod = Strange.MOD_ID)
public class Ruins extends CharmModule {
    public static boolean stoneRuins = true;

    public static int stoneRuinSize = 5;

    public static List<String> stoneRuinBiomeCategories = Arrays.asList(
        "plains", "desert", "mountains", "savanna", "forest", "icy", "mesa"
    );

    public static final List<IRuinsTheme> THEMES = new ArrayList<>();

    @Override
    public void register() {
        if (stoneRuins) {
            THEMES.add(new StoneRuins());
        }

        THEMES.forEach(IRuinsTheme::register);
    }

    @Override
    public void runWhenEnabled() {
        THEMES.forEach(IRuinsTheme::runWhenEnabled);
    }
}
