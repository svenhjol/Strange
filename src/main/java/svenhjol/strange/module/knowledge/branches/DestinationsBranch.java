package svenhjol.strange.module.knowledge.branches;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.module.knowledge.Destination;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.KnowledgeHelper;

import java.util.Optional;

public class DestinationsBranch extends KnowledgeBranch<Destination, Destination> {
    @Override
    public String getBranchName() {
        return "Destinations";
    }

    @Override
    public char getStartRune() {
        return KnowledgeHelper.getCharFromRange(Knowledge.NOVICE_RUNES, 2);
    }

    @Override
    public void register(Destination type) {
        String runes = type.getRunes();
        add(runes, type);
    }

    public static DestinationsBranch load(CompoundTag tag) {
        DestinationsBranch branch = new DestinationsBranch();
        CompoundTag map = tag.getCompound(branch.getBranchName());
        map.getAllKeys().forEach(runes -> branch.add(runes, Destination.fromTag(map.getCompound(runes))));
        return branch;
    }

    @Override
    public Tag tagify(Destination value) {
        return value.toTag();
    }

    @Override
    public Optional<String> getPrettyName(String runes) {
        return get(runes).map(destination -> StringHelper.snakeToPretty(destination.location.getPath()));
    }
}
