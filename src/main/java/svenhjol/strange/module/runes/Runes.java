package svenhjol.strange.module.runes;

import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.knowledge.command.arg.RuneArgType;

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

    @Override
    public void register() {
        ArgumentTypes.register("rune", RuneArgType.class, new EmptyArgumentSerializer<>(RuneArgType::new));
    }

    public static void addBranch(RuneBranch<?, ?> branch) {
        BRANCHES.put(branch.getBranchName(), branch);
        LogHelper.debug(Strange.MOD_ID, Runes.class, "Updated branch `" + branch.getBranchName() + "`.");
    }

    public static Map<String, RuneBranch<?, ?>> getBranches() {
        return BRANCHES;
    }
}
