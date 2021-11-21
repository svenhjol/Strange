package svenhjol.strange.module.journals;

import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.branches.BiomesBranch;
import svenhjol.strange.module.knowledge.branches.DimensionsBranch;
import svenhjol.strange.module.knowledge.branches.StructuresBranch;

import java.util.List;

public class JournalHelper {
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

    public static boolean hasLearnedAllTierRunes(int tier, JournalData journal) {
        int learnable = getNextLearnableRune(tier, journal);
        return learnable == Integer.MIN_VALUE;
    }

    public static int getNextLearnableRune(int tier, JournalData journal) {
        List<Integer> learnedRunes = journal.getLearnedRunes();
        Knowledge.Tier knowledgeTier = Knowledge.Tier.getByOrdinal(tier);
        if (knowledgeTier != null) {
            String runeset = Knowledge.TIER_RUNE_SETS.get(knowledgeTier);

            for (int i = 0; i < runeset.length(); i++) {
                char c = runeset.charAt(0);
                int intval = (int) c - 97;
                if (!learnedRunes.contains(intval)) {
                    return intval;
                }
            }
        }

        return Integer.MIN_VALUE;
    }

    public static void tryLearnPhrase(String runes, JournalData journal) {
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
