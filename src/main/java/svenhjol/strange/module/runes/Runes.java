package svenhjol.strange.module.runes;

import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

@CommonModule(mod = Strange.MOD_ID, alwaysEnabled = true)
public class Runes extends CharmModule {
    public static final int NUM_RUNES = 26;

    public static final int MAX_PHRASE_LENGTH = 23;
    public static final int MIN_PHRASE_LENGTH = 5;

    public static final char FIRST_RUNE = 'a';
    public static final char LAST_RUNE = 'z';
    public static final char UNKNOWN_RUNE = '?';

    private static final Map<String, RuneBranch<?, ?>> BRANCHES = new HashMap<>();

    public static void addBranch(RuneBranch<?, ?> branch) {
        BRANCHES.put(branch.getBranchName(), branch);
        LogHelper.debug(Runes.class, "Added branch `" + branch.getBranchName() + "`.");
    }

    public static Map<String, RuneBranch<?, ?>> getBranches() {
        return BRANCHES;
    }
}
