package svenhjol.strange.module.ruins.generator;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import svenhjol.charm.world.CharmGenerator;
import svenhjol.charm.world.CharmStructure;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;

public class CaveRuinGenerator extends CharmGenerator {
    public static StructureTemplatePool POOL;
    public static StructureTemplatePool EMPTY_POOL;
    public static List<CharmStructure> RUINS = new ArrayList<>();

    public static void init() {
        POOL = registerPool(new ResourceLocation(Strange.MOD_ID, "ruins/cave/starts"), RUINS);
        EMPTY_POOL = emptyPool(new ResourceLocation(Strange.MOD_ID, "ruins/empty/starts"));
    }
}
