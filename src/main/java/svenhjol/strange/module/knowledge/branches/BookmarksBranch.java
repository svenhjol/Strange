package svenhjol.strange.module.knowledge.branches;

import net.minecraft.nbt.Tag;
import svenhjol.strange.module.journals.JournalData;
import svenhjol.strange.module.journals.JournalBookmark;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.KnowledgeHelper;

import java.util.Optional;

public class BookmarksBranch extends KnowledgeBranch<String, JournalBookmark> {
    @Override
    public void register(String type) {
        // you can't register a JournalLocation to the global scope, so do nothing here
    }

    @Override
    public void add(String runes, JournalBookmark value) {
        // you can't add a JournalLocation to the global scope, so do nothing here
    }

    @Override
    public Optional<JournalBookmark> get(String runes) {
        return JournalData.getBookmarkByRunes(runes);
    }

    @Override
    public boolean has(String runes) {
        return get(runes).isPresent();
    }

    @Override
    protected Tag tagify(JournalBookmark value) {
        return null;
    }

    @Override
    public String getBranchName() {
        return "Locations";
    }

    @Override
    public Optional<String> getPrettyName(String runes) {
        return JournalData.getBookmarkByRunes(runes).map(JournalBookmark::getName);
    }

    @Override
    public char getStartRune() {
        return KnowledgeHelper.getCharFromRange(Knowledge.NOVICE_RUNES, 1);
    }
}
