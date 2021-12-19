package svenhjol.strange.module.journals2.helper;

import svenhjol.strange.module.journals2.Journal2Data;
import svenhjol.strange.module.journals2.Journals2Client;
import svenhjol.strange.module.runes.Tier;

import java.util.List;

public class Journal2Helper {
    public static List<Integer> getLearnedRunes() {
        if (Journals2Client.journal == null) return List.of();
        return Journals2Client.journal.getLearnedRunes();
    }

    public static int nextLearnableRune(Tier currentTier, Journal2Data journal) {
        var learnedRunes = journal.getLearnedRunes();

        for (int t = 1; t <= currentTier.ordinal(); t++) {
            var tier = Tier.getByOrdinal(t);
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
}
