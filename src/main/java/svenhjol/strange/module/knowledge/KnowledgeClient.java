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

@ClientModule(module = Knowledge.class)
public class KnowledgeClient extends CharmModule {
    public static @Nullable BiomeBranch biomes;
    public static @Nullable DimensionBranch dimensions;
    public static @Nullable StructureBranch structures;

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
}
