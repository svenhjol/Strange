package svenhjol.strange.module.knowledge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import svenhjol.strange.module.knowledge.branch.BiomeBranch;
import svenhjol.strange.module.knowledge.branch.DimensionBranch;
import svenhjol.strange.module.knowledge.branch.StructureBranch;

import javax.annotation.Nullable;

public class KnowledgeData extends SavedData {
    public static final String SEED_TAG = "Seed";

    public BiomeBranch biomeBranch;
    public DimensionBranch dimensionBranch;
    public StructureBranch structureBranch;

    public KnowledgeData(@Nullable ServerLevel level) {
        this.setDirty();

        biomeBranch = new BiomeBranch();
        dimensionBranch = new DimensionBranch();
        structureBranch = new StructureBranch();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong(SEED_TAG, Knowledge.SEED);

        biomeBranch.save(tag);
        dimensionBranch.save(tag);
        structureBranch.save(tag);

        return tag;
    }

    public static KnowledgeData load(CompoundTag tag) {
        return load(null, tag);
    }

    public static KnowledgeData load(@Nullable ServerLevel level, CompoundTag tag) {
        KnowledgeData knowledge = new KnowledgeData(level);

        knowledge.biomeBranch = BiomeBranch.load(tag);
        knowledge.dimensionBranch = DimensionBranch.load(tag);
        knowledge.structureBranch = StructureBranch.load(tag);

        return knowledge;
    }

    public static String getFileId(DimensionType dimensionType) {
        return "strange_knowledge" + dimensionType.getFileSuffix();
    }
}
