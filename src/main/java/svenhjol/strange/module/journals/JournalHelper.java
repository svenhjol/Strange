package svenhjol.strange.module.journals;

import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.Knowledge.Tier;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.branches.BiomesBranch;
import svenhjol.strange.module.knowledge.branches.DimensionsBranch;
import svenhjol.strange.module.knowledge.branches.StructuresBranch;

import java.util.List;

public class JournalHelper {
    public static boolean hasLearnedAnyRunes(JournalData journal) {
        return !journal.getLearnedRunes().isEmpty();
    }

    public static int getNumberOfUnknownRunes(String runes, JournalData journal) {
        int num = 0;

        for (int i = 0; i < runes.length(); i++) {
            int chr = runes.charAt(i) - 97;
            if (!journal.getLearnedRunes().contains(chr)) {
                num++;
            }
        }

        return num;
    }

    public static int getNextLearnableRune(JournalData journal) {
        return getNextLearnableRune(Tier.MASTER.ordinal(), journal);
    }

    public static int getNextLearnableRune(int tier, JournalData journal) {
        List<Integer> learnedRunes = journal.getLearnedRunes();

        for (int t = 1; t <= tier; t++) {
            Tier knowledgeTier = Tier.getByOrdinal(t);

            if (knowledgeTier != null) {
                String runeset = Knowledge.TIER_RUNE_SETS.get(knowledgeTier);

                for (int i = 0; i < runeset.length(); i++) {
                    char c = runeset.charAt(i);
                    int intval = (int) c - 97;
                    if (!learnedRunes.contains(intval)) {
                        return intval;
                    }
                }
            }
        }

        return Integer.MIN_VALUE;
    }

    public static boolean learnNextLearnableRune(JournalData journal) {
        return learnNextLearnableRune(Tier.MASTER.ordinal(), journal);
    }

    public static boolean learnNextLearnableRune(int tier, JournalData journal) {
        int learnable = getNextLearnableRune(tier, journal);

        if (learnable >= 0) {
            journal.learnRune(learnable);
            return true;
        }

        return false;
    }

    public static void tryLearnPhrase(String runes, JournalData journal) {
        if (JournalHelper.getNumberOfUnknownRunes(runes, journal) > 0) {
            return;
        }

        KnowledgeBranch.getByStartRune(runes.charAt(0)).ifPresent(branch -> {
            if (branch.isLearnable()) {
                switch (branch.getBranchName()) {
                    case BiomesBranch.NAME -> ((BiomesBranch) branch).get(runes).ifPresent(journal::learnBiome);
                    case DimensionsBranch.NAME -> ((DimensionsBranch) branch).get(runes).ifPresent(journal::learnDimension);
                    case StructuresBranch.NAME -> ((StructuresBranch) branch).get(runes).ifPresent(journal::learnStructure);
                }
            }
        });
    }
}
