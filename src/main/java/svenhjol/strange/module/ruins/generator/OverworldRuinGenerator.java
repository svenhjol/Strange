package svenhjol.strange.module.ruins.generator;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import svenhjol.strange.Strange;
import svenhjol.strange.module.structures.BaseGenerator;
import svenhjol.strange.module.structures.BaseStructure;

import java.util.ArrayList;
import java.util.List;

public class OverworldRuinGenerator extends BaseGenerator {
    public static StructureTemplatePool POOL;
    public static List<BaseStructure> RUINS = new ArrayList<>();

    public static void init() {
        POOL = registerPool(new ResourceLocation(Strange.MOD_ID, "ruins/starts"), RUINS);
    }
}
