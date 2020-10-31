package svenhjol.strange.foundations;

import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Identifier;
import svenhjol.charm.base.structure.BaseGenerator;
import svenhjol.charm.base.structure.BaseStructure;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;

public class FoundationGenerator extends BaseGenerator {
    public static StructurePool FOUNDATION_POOL;

    public static List<BaseStructure> FOUNDATIONS = new ArrayList<>();

    public static void init() {
        FOUNDATION_POOL = registerPool(new Identifier(Strange.MOD_ID, "foundations/overworld/starts"), FOUNDATIONS);
    }
}
