package svenhjol.strange.ruins;

import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Identifier;
import svenhjol.charm.base.structure.BaseGenerator;
import svenhjol.charm.base.structure.BaseStructure;
import svenhjol.strange.Strange;

import java.util.ArrayList;
import java.util.List;

public class SurfaceRuinGenerator extends BaseGenerator {
    public static StructurePool BADLANDS_POOL;
    public static StructurePool DESERT_POOL;
    public static StructurePool END_POOL;
    public static StructurePool FOREST_POOL;
    public static StructurePool JUNGLE_POOL;
    public static StructurePool MOUNTAINS_POOL;
    public static StructurePool OCEAN_POOL;
    public static StructurePool PLAINS_POOL;
    public static StructurePool SAVANNA_POOL;
    public static StructurePool SNOWY_POOL;
    public static StructurePool TAIGA_POOL;

    public static List<BaseStructure> BADLANDS_RUINS = new ArrayList<>();
    public static List<BaseStructure> DESERT_RUINS = new ArrayList<>();
    public static List<BaseStructure> END_RUINS = new ArrayList<>();
    public static List<BaseStructure> FOREST_RUINS = new ArrayList<>();
    public static List<BaseStructure> JUNGLE_RUINS = new ArrayList<>();
    public static List<BaseStructure> MOUNTAINS_RUINS = new ArrayList<>();
    public static List<BaseStructure> OCEAN_RUINS = new ArrayList<>();
    public static List<BaseStructure> PLAINS_RUINS = new ArrayList<>();
    public static List<BaseStructure> SAVANNA_RUINS = new ArrayList<>();
    public static List<BaseStructure> SNOWY_RUINS = new ArrayList<>();
    public static List<BaseStructure> TAIGA_RUINS = new ArrayList<>();

    public static void init() {
        StructurePool.Projection projection = StructurePool.Projection.TERRAIN_MATCHING;

        BADLANDS_POOL = registerPool(new Identifier(Strange.MOD_ID, "ruins/surface/badlands/starts"), BADLANDS_RUINS, projection);
        DESERT_POOL = registerPool(new Identifier(Strange.MOD_ID, "ruins/surface/desert/starts"), DESERT_RUINS, projection);
        END_POOL = registerPool(new Identifier(Strange.MOD_ID, "ruins/surface/end/starts"), END_RUINS, projection);
        FOREST_POOL = registerPool(new Identifier(Strange.MOD_ID, "ruins/surface/forest/starts"), FOREST_RUINS, projection);
        JUNGLE_POOL = registerPool(new Identifier(Strange.MOD_ID, "ruins/surface/jungle/starts"), JUNGLE_RUINS, projection);
        MOUNTAINS_POOL = registerPool(new Identifier(Strange.MOD_ID, "ruins/surface/mountains/starts"), MOUNTAINS_RUINS, projection);
        OCEAN_POOL = registerPool(new Identifier(Strange.MOD_ID, "ruins/surface/ocean/starts"), OCEAN_RUINS, projection);
        PLAINS_POOL = registerPool(new Identifier(Strange.MOD_ID, "ruins/surface/plains/starts"), PLAINS_RUINS, projection);
        SAVANNA_POOL = registerPool(new Identifier(Strange.MOD_ID, "ruins/surface/savanna/starts"), SAVANNA_RUINS, projection);
        SNOWY_POOL = registerPool(new Identifier(Strange.MOD_ID, "ruins/surface/snowy/starts"), SNOWY_RUINS, projection);
        TAIGA_POOL = registerPool(new Identifier(Strange.MOD_ID, "ruins/surface/taiga/starts"), TAIGA_RUINS, projection);
    }
}
