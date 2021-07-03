package svenhjol.strange.module.ruins.generator;

import svenhjol.charm.world.CharmGenerator;
import svenhjol.charm.world.CharmStructure;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;

public class DeepRuinGenerator extends CharmGenerator {
    public static StructureTemplatePool POOL;

    public static List<CharmStructure> RUINS = new ArrayList<>();

    public static void init() {
        POOL = registerPool(new ResourceLocation(Strange.MOD_ID, "ruins/deep/starts"), RUINS);
    }
}
