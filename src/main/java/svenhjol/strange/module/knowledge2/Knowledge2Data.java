package svenhjol.strange.module.knowledge2;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import svenhjol.strange.module.knowledge2.branch.BiomeBranch;
import svenhjol.strange.module.knowledge2.branch.DimensionBranch;
import svenhjol.strange.module.knowledge2.branch.StructureBranch;

import javax.annotation.Nullable;

public class Knowledge2Data extends SavedData {
    public static final String SEED_TAG = "Seed";

    public BiomeBranch biomeBranch;
    public DimensionBranch dimensionBranch;
    public StructureBranch structureBranch;

    public Knowledge2Data(@Nullable ServerLevel level) {
        this.setDirty();

        biomeBranch = new BiomeBranch();
        dimensionBranch = new DimensionBranch();
        structureBranch = new StructureBranch();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong(SEED_TAG, Knowledge2.SEED);

        biomeBranch.save(tag);
        dimensionBranch.save(tag);
        structureBranch.save(tag);

        return tag;
    }

    public static Knowledge2Data load(CompoundTag tag) {
        return load(null, tag);
    }

    public static Knowledge2Data load(@Nullable ServerLevel level, CompoundTag tag) {
        Knowledge2Data knowledge = new Knowledge2Data(level);

        knowledge.biomeBranch = BiomeBranch.load(tag);
        knowledge.dimensionBranch = DimensionBranch.load(tag);
        knowledge.structureBranch = StructureBranch.load(tag);

        return knowledge;
    }

    public static String getFileId(DimensionType dimensionType) {
        return "strange_knowledge2" + dimensionType.getFileSuffix();
    }
}
