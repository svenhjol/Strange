package svenhjol.strange.structure;

import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Identifier;
import svenhjol.charm.base.structure.BaseGenerator;
import svenhjol.charm.base.structure.BaseStructure;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;

public class AncientRuinGenerator extends BaseGenerator {
    public static StructurePool OVERWORLD_POOL;

    public static List<BaseStructure> OVERWORLD_RUINS = new ArrayList<>();

    public static void init() {
        OVERWORLD_POOL = registerPool(new Identifier(Strange.MOD_ID, "ancient_ruins/overworld/starts"), OVERWORLD_RUINS);
    }
}
