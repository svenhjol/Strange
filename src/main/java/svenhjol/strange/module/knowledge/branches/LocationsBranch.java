package svenhjol.strange.module.knowledge.branches;

import net.minecraft.nbt.Tag;
import svenhjol.strange.module.journals.JournalData;
import svenhjol.strange.module.journals.data.JournalLocation;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.KnowledgeHelper;

import java.util.Optional;

public class LocationsBranch extends KnowledgeBranch<String, JournalLocation> {
    @Override
    public void register(String type) {
        // you can't register a JournalLocation to the global scope, so do nothing here
    }

    @Override
    public void add(String runes, JournalLocation value) {
        // you can't add a JournalLocation to the global scope, so do nothing here
    }

    @Override
    public Optional<JournalLocation> get(String runes) {
        return JournalData.getLocationByRunes(runes);
    }

    @Override
    public boolean has(String runes) {
        return get(runes).isPresent();
    }

    @Override
    public Tag tagify(JournalLocation value) {
        return null;
    }

    @Override
    public String getBranchName() {
        return "Locations";
    }

    @Override
    public Optional<String> getPrettyName(String runes) {
        return JournalData.getLocationByRunes(runes).map(JournalLocation::getName);
    }

    @Override
    public char getStartRune() {
        return KnowledgeHelper.getCharFromRange(Knowledge.NOVICE_RUNES, 1);
    }
}
