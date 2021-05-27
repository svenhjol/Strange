package svenhjol.strange.world.gen;

import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Identifier;
import svenhjol.charm.base.structure.BaseGenerator;
import svenhjol.charm.base.structure.BaseStructure;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;

public class SurfaceRuinGenerator extends BaseGenerator {
    public static StructurePool POOL;
    public static List<BaseStructure> RUINS = new ArrayList<>();

    public static void init() {
        StructurePool.Projection projection = StructurePool.Projection.TERRAIN_MATCHING;
        POOL = registerPool(new Identifier(Strange.MOD_ID, "ruins/surface/starts"), RUINS, projection);
    }
}
