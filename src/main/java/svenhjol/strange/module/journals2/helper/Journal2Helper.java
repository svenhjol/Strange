package svenhjol.strange.module.journals2.helper;

import svenhjol.strange.module.journals2.Journals2Client;
import svenhjol.strange.module.runes.RuneBranch;
import svenhjol.strange.module.runes.Tier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class Journal2Helper {
    public static List<Integer> getLearnedRunes() {
        if (Journals2Client.journal == null) return List.of();
        return Journals2Client.journal.getLearnedRunes();
    }

    public static int countUnknownRunes(String runes) {
        if (Journals2Client.journal == null) return 0;

        int num = 0;

        for (int i = 0; i < runes.length(); i++) {
            int chr = runes.charAt(i) - 97;
            if (!Journals2Client.journal.getLearnedRunes().contains(chr)) {
                num++;
            }
        }

        return num;
    }

    public static int nextLearnableRune(Tier currentTier) {
        if (Journals2Client.journal != null) {
            var learnedRunes = Journals2Client.journal.getLearnedRunes();

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
        }

        return Integer.MIN_VALUE;
    }

    public static <T> boolean learn(RuneBranch<?, T> branch, List<T> existingKnowledge, Function<T, Boolean> onLearn) {
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
}
