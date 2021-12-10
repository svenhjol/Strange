package svenhjol.strange.module.overworld_ruins.generator;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import svenhjol.strange.Strange;
import svenhjol.strange.structure.StrangeGenerator;
import svenhjol.strange.structure.StrangeStructure;

import java.util.ArrayList;
import java.util.List;

public class OverworldRuinGenerator extends StrangeGenerator {
    public static StructureTemplatePool POOL;
    public static List<StrangeStructure> RUINS = new ArrayList<>();

    public static void init() {
        POOL = registerPool(new ResourceLocation(Strange.MOD_ID, "ruins/starts"), RUINS);
    }
}
