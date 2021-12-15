package svenhjol.strange.module.knowledge.branches;

import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import svenhjol.strange.module.journals.JournalData;
import svenhjol.strange.module.journals.JournalBookmark;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.Knowledge.Tier;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.KnowledgeHelper;

import java.util.Optional;

public class BookmarksBranch extends KnowledgeBranch<String, JournalBookmark> {
    public static final String NAME = "Bookmarks";

    @Override
    public void register(String type) {
        // you can't register a JournalBookmark to the global scope, so do nothing here
    }

    @Override
    public void add(String runes, JournalBookmark value) {
        // you can't add a JournalBookmark to the global scope, so do nothing here
    }

    @Override
    public boolean isLearnable() {
        return false;
    }

    @Override
    public Optional<JournalBookmark> get(String runes) {
        return JournalData.getBookmarkByRunes(runes);
    }

    /**
     * Any sequence of bookmark runes are valid, so return true as long as the string length is within bounds.
     */
    @Override
    public boolean has(String runes) {
        return !runes.isEmpty() && runes.length() < Knowledge.MAX_LENGTH;
    }

    @Override
    protected Tag tagify(JournalBookmark value) {
        return null;
    }

    @Override
    public String getBranchName() {
        return NAME;
    }

    @Override
    public Optional<String> getPrettyName(String runes) {
        return JournalData.getBookmarkByRunes(runes).map(JournalBookmark::getName);
    }

    public Optional<String> getPrettyName(String runes, Player player) {
        return Journals.getJournalData(player)
            .flatMap(journal -> journal.getBookmark(runes).map(JournalBookmark::getName));
    }

    @Override
    public char getStartRune() {
        return KnowledgeHelper.getCharFromRange(Knowledge.TIER_RUNE_SETS.get(Tier.NOVICE), 1);
    }
}
