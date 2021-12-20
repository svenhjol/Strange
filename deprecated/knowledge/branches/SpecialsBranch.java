package svenhjol.strange.module.knowledge.branches;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.knowledge.Knowledge.Tier;
import svenhjol.strange.module.knowledge.KnowledgeBranch;
import svenhjol.strange.module.knowledge.KnowledgeHelper;
import svenhjol.strange.module.discoveries.Discovery;

import java.util.Optional;

public class SpecialsBranch extends KnowledgeBranch<Discovery, Discovery> {
    public static final String NAME = "Specials";

    @Override
    public void register(Discovery type) {
        add(type.getRunes(), type);
    }

    @Override
    protected Tag tagify(Discovery value) {
        return value.save();
    }

    @Override
    public String getBranchName() {
        return NAME;
    }

    @Override
    public boolean isLearnable() {
        return false;
    }

    @Override
    public char getStartRune() {
        return KnowledgeHelper.getCharFromRange(Knowledge.TIER_RUNE_SETS.get(Tier.NOVICE), 0);
    }

    public static SpecialsBranch load(CompoundTag tag) {
        SpecialsBranch branch = new SpecialsBranch();
        CompoundTag map = tag.getCompound(branch.getBranchName());
        map.getAllKeys().forEach(runes -> branch.add(runes, Discovery.load(map.getCompound(runes))));
        return branch;
    }

    @Override
    public Optional<String> getPrettyName(String runes) {
        return get(runes).map(destination -> StringHelper.snakeToPretty(destination.getLocation().getPath()));
    }
}
