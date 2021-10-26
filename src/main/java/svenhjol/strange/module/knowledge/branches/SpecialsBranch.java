package svenhjol.strange.module.knowledge.branches;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.module.knowledge.Destination;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.KnowledgeHelper;

import java.util.Optional;

public class SpecialsBranch extends KnowledgeBranch<Destination, Destination> {
    @Override
    public void register(Destination type) {
        add(type.getRunes(), type);
    }

    @Override
    public Tag tagify(Destination value) {
        return value.toTag();
    }

    @Override
    public String getBranchName() {
        return "Specials";
    }

    @Override
    public char getStartRune() {
        return KnowledgeHelper.getCharFromRange(Knowledge.NOVICE_RUNES, 0);
    }

    public static SpecialsBranch load(CompoundTag tag) {
        SpecialsBranch branch = new SpecialsBranch();
        CompoundTag map = tag.getCompound(branch.getBranchName());
        map.getAllKeys().forEach(runes -> branch.add(runes, Destination.fromTag(map.getCompound(runes))));
        return branch;
    }

    @Override
    public Optional<String> getPrettyName(String runes) {
        return get(runes).map(destination -> StringHelper.snakeToPretty(destination.location.getPath()));
    }
}