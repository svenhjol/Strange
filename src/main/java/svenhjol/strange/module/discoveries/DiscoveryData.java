package svenhjol.strange.module.discoveries;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;

public class DiscoveryData extends SavedData {
    public DiscoveryBranch branch;

    public DiscoveryData(@Nullable ServerLevel level) {
        this.setDirty();
        branch = new DiscoveryBranch();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        return null;
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
