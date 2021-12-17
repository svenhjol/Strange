package svenhjol.strange.module.knowledge.branches;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import svenhjol.strange.module.journals.JournalBookmark;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.Knowledge.Tier;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.KnowledgeHelper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class BookmarksBranch extends KnowledgeBranch<JournalBookmark, JournalBookmark> {
    public static final String NAME = "Bookmarks";

    @Override
    public void register(JournalBookmark bookmark) {
        // Don't allow more than the maximum number of bookmarks per player!
        // It's possible to bypass this by using the add() method directly, but don't.
        if (allByUuid(bookmark.getUuid()).size() < Journals.maxBookmarksPerPlayer) {
            String runes = bookmark.getRunes();
            add(runes, bookmark);
        }
    }

    @Override
    public boolean isLearnable() {
        return false;
    }

    @Override
    protected Tag tagify(JournalBookmark value) {
        return value.toTag();
    }

    @Override
    public String getBranchName() {
        return NAME;
    }

    @Override
    public Optional<String> getPrettyName(String runes) {
        return get(runes).map(JournalBookmark::getName);
    }

    @Override
    public char getStartRune() {
        return KnowledgeHelper.getCharFromRange(Knowledge.TIER_RUNE_SETS.get(Tier.NOVICE), 1);
    }

    public List<JournalBookmark> allByUuid(UUID uuid) {
        return values().stream().filter(b -> b.getUuid().equals(uuid)).collect(Collectors.toList());
    }

    public static BookmarksBranch load(CompoundTag tag) {
        BookmarksBranch branch = new BookmarksBranch();
        CompoundTag map = tag.getCompound(branch.getBranchName());
        map.getAllKeys().forEach(runes -> branch.add(runes, JournalBookmark.fromTag(map.getCompound(runes))));
        return branch;
    }
}
