package svenhjol.strange.module.journals;

public class JournalHelper {
    public static int getNumberOfUnknownRunes(String runes, JournalData playerJournal) {
        int num = 0;

        for (int i = 0; i < runes.length(); i++) {
            int chr = runes.charAt(i) - 97;
            if (!playerJournal.getLearnedRunes().contains(chr)) {
                num++;
            }
        }

        return num;
    }
}
