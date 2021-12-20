package svenhjol.strange.module.discoveries;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.List;

public class DiscoveryData extends SavedData {
    public DiscoveryBranch branch;

    public DiscoveryData(@Nullable ServerLevel level) {
        this.setDirty();
        branch = new DiscoveryBranch();
    }

    public Discovery add(Discovery discovery) {
        branch.register(discovery);

        this.setDirty();
        return discovery;
    }

    public List<Discovery> all() {
        return branch.values();
    }

    public boolean contains(String runes) {
        return branch.contains(runes);
    }

    public boolean contains(Discovery discovery) {
        return branch.contains(discovery);
    }

    public Discovery get(String runes) {
        return branch.get(runes);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        branch.save(tag);
        return tag;
    }

    public static DiscoveryData load(CompoundTag tag) {
        return load(null, tag);
    }

    public static DiscoveryData load(@Nullable ServerLevel level, CompoundTag tag) {
        var discoveries = new DiscoveryData(level);
        discoveries.branch = DiscoveryBranch.load(tag);
        return discoveries;
    }

    public static String getFileId(DimensionType dimensionType) {
        return "strange_discoveries" + dimensionType.getFileSuffix();
    }
}
