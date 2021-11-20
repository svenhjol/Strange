package svenhjol.strange.module.journals;

import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.branches.BiomesBranch;
import svenhjol.strange.module.knowledge.branches.DimensionsBranch;
import svenhjol.strange.module.knowledge.branches.StructuresBranch;

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

    public static void tryLearn(String runes, JournalData journal) {
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
