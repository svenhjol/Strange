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

public class DiscoveriesBranch extends KnowledgeBranch<Discovery, Discovery> {
    public static final String NAME = "Discoveries";

    @Override
    public String getBranchName() {
        return NAME;
    }

    @Override
    public char getStartRune() {
        return KnowledgeHelper.getCharFromRange(Knowledge.TIER_RUNE_SETS.get(Tier.NOVICE), 2);
    }

    @Override
    public boolean isLearnable() {
        return false;
    }

    @Override
    public void register(Discovery discovery) {
        String runes = discovery.getRunes();
        add(runes, discovery);
    }

    public static DiscoveriesBranch load(CompoundTag tag) {
        DiscoveriesBranch branch = new DiscoveriesBranch();
        CompoundTag map = tag.getCompound(branch.getBranchName());
        map.getAllKeys().forEach(runes -> branch.add(runes, Discovery.load(map.getCompound(runes))));
        return branch;
    }

    @Override
    protected Tag tagify(Discovery value) {
        return value.save();
    }

    @Override
    public Optional<String> getPrettyName(String runes) {
        return get(runes).map(discovery -> StringHelper.snakeToPretty(discovery.getLocation().getPath()));
    }
}