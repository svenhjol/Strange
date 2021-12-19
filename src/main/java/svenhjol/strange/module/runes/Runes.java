package svenhjol.strange.module.runes;

import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;

@CommonModule(mod = Strange.MOD_ID, alwaysEnabled = true)
public class Runes extends CharmModule {
    public static final int NUM_RUNES = 26;

    public static final int MAX_PHRASE_LENGTH = 23;
    public static final int MIN_PHRASE_LENGTH = 5;

    public static final char FIRST_RUNE = 'a';
    public static final char LAST_RUNE = 'z';
}
