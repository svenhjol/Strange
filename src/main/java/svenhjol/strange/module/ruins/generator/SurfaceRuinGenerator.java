package svenhjol.strange.module.ruins.generator;

import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Identifier;
import svenhjol.charm.world.CharmGenerator;
import svenhjol.charm.world.CharmStructure;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;

public class SurfaceRuinGenerator extends CharmGenerator {
    public static StructurePool POOL;
    public static List<CharmStructure> RUINS = new ArrayList<>();

    public static void init() {
        StructurePool.Projection projection = StructurePool.Projection.TERRAIN_MATCHING;
        POOL = registerPool(new Identifier(Strange.MOD_ID, "ruins/surface/starts"), RUINS, projection);
    }
}
