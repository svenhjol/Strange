package svenhjol.strange.module.discoveries;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.module.runes.RuneBranch;
import svenhjol.strange.module.runes.RuneHelper;
import svenhjol.strange.module.runes.Tier;

public class DiscoveryBranch extends RuneBranch<Discovery, Discovery> {
    public static final String NAME = "Discoveries";

    @Override
    public Discovery register(Discovery discovery) {
        var runes = discovery.getRunes();
        add(runes, discovery);
        return discovery;
    }

    @Override
    public Tag getValueTag(Discovery discovery) {
        return discovery.save();
    }

    @Override
    public char getStartRune() {
        return RuneHelper.getFromRuneSet(Tier.NOVICE, 2);
    }

    @Override
    public @Nullable String getValueName(String runes) {
        var discovery = get(runes);
        return discovery != null ? StringHelper.snakeToPretty(discovery.getLocation().getPath()) : null;
    }

    @Override
    public String getBranchName() {
        return NAME;
    }

    public static DiscoveryBranch load(CompoundTag tag) {
        var branch = new DiscoveryBranch();
        var map = tag.getCompound(branch.getBranchName());
        map.getAllKeys().forEach(runes -> branch.add(runes, Discovery.load(map.getCompound(runes))));
        return branch;
    }
}
