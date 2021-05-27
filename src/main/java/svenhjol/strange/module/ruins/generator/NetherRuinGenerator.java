package svenhjol.strange.module.ruins.generator;

import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Identifier;
import svenhjol.charm.world.CharmGenerator;
import svenhjol.charm.world.CharmStructure;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;

public class NetherRuinGenerator extends CharmGenerator {
    public static StructurePool POOL;

    public static List<CharmStructure> RUINS = new ArrayList<>();

    public static void init() {
        POOL = registerPool(new Identifier(Strange.MOD_ID, "ruins/nether/starts"), RUINS);
    }
}
