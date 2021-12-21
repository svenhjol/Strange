package svenhjol.strange.module.journals.helper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.strange.module.journals.JournalData;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.knowledge.branch.BiomeBranch;
import svenhjol.strange.module.knowledge.branch.DimensionBranch;
import svenhjol.strange.module.knowledge.branch.StructureBranch;
import svenhjol.strange.module.runes.RuneBranch;
import svenhjol.strange.module.runes.RuneHelper;
import svenhjol.strange.module.runes.Tier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class JournalHelper {
    /**
     * Returns an array containing the int value of all the runes a player has learned.
     * This method is designed to be run on both sides.
     */
    public static List<Integer> getLearnedRunes(JournalData journal) {
        return journal.getLearnedRunes();
    }

    /**
     * Returns the number of runes that a player has not learned within a given string.
     * This method is designed to be run on both sides.
     */
    public static int countUnknownRunes(String runes, JournalData journal) {
        int num = 0;

        for (int i = 0; i < runes.length(); i++) {
            int chr = runes.charAt(i) - 97;
            if (!journal.getLearnedRunes().contains(chr)) {
                num++;
            }
        }

        return num;
    }

    /**
     * Returns the next learnable rune value within the given tier (or lower).
     * This method is designed to be run on both sides.
     */
    public static int nextLearnableRune(Tier currentTier, JournalData journal) {
        var learnedRunes = journal.getLearnedRunes();

        for (int t = 1; t <= currentTier.getLevel(); t++) {
            var tier = Tier.byLevel(t);
            if (tier == null) continue;
            var chars = tier.getChars();

            for (char c : chars) {
                int intval = (int) c - 97;
                if (!learnedRunes.contains(intval)) {
                    return intval;
                }
            }
        }

        return Integer.MIN_VALUE;
    }

    /**
     * Try to learn a completely new rune phrase within a given branch.
     * ExistingKnowledge is checked so that a new phrase can be found.
     * While this technically works on both sides you want to run it on the server.
     */
    public static <T> boolean tryLearn(RuneBranch<?, T> branch, List<T> existingKnowledge, Function<T, Boolean> onLearn) {
        // If the current knowledge is less than the knowledge in the branch then there's something to be learned.
        if (existingKnowledge.size() < branch.size()) {
            var list = new ArrayList<>(branch.values());
            Collections.shuffle(list, new Random());

            for (T item : list) {
                if (!existingKnowledge.contains(item)) {
                    return onLearn.apply(item);
                }
            }
        }

        // Did not learn anything.
        return false;
    }

    /**
     * If the player doesn't know the thing that the given rune phrase represents, then try and learn it.
     * This runs on the server side so that the learning can be backed.
     */
    public static boolean tryLearn(ServerPlayer player, String runes) {
        var journal = Journals.getJournal(player).orElse(null);
        if (journal == null) return false;

        var branch = RuneHelper.branch(runes);
        if (branch == null) return false;

        if (!(branch.get(runes) instanceof ResourceLocation id)) return false;

        boolean learned = false;

        switch (branch.getBranchName()) {
            case BiomeBranch.NAME -> {
                if (!journal.getLearnedBiomes().contains(id)) {
                    journal.learnBiome(id);
                    learned = true;
                }
            }
            case DimensionBranch.NAME -> {
                if (!journal.getLearnedDimensions().contains(id)) {
                    journal.learnDimension(id);
                    learned = true;
                }
            }
            case StructureBranch.NAME -> {
                if (!journal.getLearnedStructures().contains(id)) {
                    journal.learnStructure(id);
                    learned = true;
                }
            }
        }

        if (learned) {
            Journals.sendJournal(player);
        }

        return learned;
    }
}
