package svenhjol.strange.module.knowledge;

import org.jetbrains.annotations.Nullable;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.knowledge.branch.BiomeBranch;
import svenhjol.strange.module.knowledge.branch.DimensionBranch;
import svenhjol.strange.module.knowledge.branch.StructureBranch;
import svenhjol.strange.module.knowledge.network.ClientReceiveBiomes;
import svenhjol.strange.module.knowledge.network.ClientReceiveDimensions;
import svenhjol.strange.module.knowledge.network.ClientReceiveSeed;
import svenhjol.strange.module.knowledge.network.ClientReceiveStructures;

import java.util.Optional;

@ClientModule(module = Knowledge.class)
public class KnowledgeClient extends CharmModule {
    private static @Nullable BiomeBranch biomes;
    private static @Nullable DimensionBranch dimensions;
    private static @Nullable StructureBranch structures;

    public static ClientReceiveSeed RECEIVE_SEED;
    public static ClientReceiveBiomes RECEIVE_BIOMES;
    public static ClientReceiveDimensions RECEIVE_DIMENSIONS;
    public static ClientReceiveStructures RECEIVE_STRUCTURES;

    @Override
    public void runWhenEnabled() {
        RECEIVE_SEED = new ClientReceiveSeed();
        RECEIVE_BIOMES = new ClientReceiveBiomes();
        RECEIVE_DIMENSIONS = new ClientReceiveDimensions();
        RECEIVE_STRUCTURES = new ClientReceiveStructures();
    }

    public static Optional<BiomeBranch> getBiomes() {
        return Optional.ofNullable(biomes);
    }

    public static Optional<DimensionBranch> getDimensions() {
        return Optional.ofNullable(dimensions);
    }

    public static Optional<StructureBranch> getStructures() {
        return Optional.ofNullable(structures);
    }

    public static void setBiomes(BiomeBranch biomes) {
        KnowledgeClient.biomes = biomes;
    }

    public static void setDimensions(DimensionBranch dimensions) {
        KnowledgeClient.dimensions = dimensions;
    }

    public static void setStructures(StructureBranch structures) {
        KnowledgeClient.structures = structures;
    }
}
