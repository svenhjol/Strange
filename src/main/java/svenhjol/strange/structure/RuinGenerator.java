package svenhjol.strange.structure;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.util.Identifier;
import svenhjol.strange.Strange;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RuinGenerator {
    public static StructurePool BADLANDS_POOL;
    public static StructurePool DESERT_POOL;
    public static StructurePool FOREST_POOL;
    public static StructurePool JUNGLE_POOL;
    public static StructurePool MOUNTAINS_POOL;
    public static StructurePool NETHER_POOL;
    public static StructurePool PLAINS_POOL;
    public static StructurePool SAVANNA_POOL;
    public static StructurePool SNOWY_POOL;
    public static StructurePool TAIGA_POOL;

    public static List<BasePiece> BADLANDS_RUINS = new ArrayList<>();
    public static List<BasePiece> DESERT_RUINS = new ArrayList<>();
    public static List<BasePiece> FOREST_RUINS = new ArrayList<>();
    public static List<BasePiece> JUNGLE_RUINS = new ArrayList<>();
    public static List<BasePiece> MOUNTAINS_RUINS = new ArrayList<>();
    public static List<BasePiece> NETHER_RUINS = new ArrayList<>();
    public static List<BasePiece> PLAINS_RUINS = new ArrayList<>();
    public static List<BasePiece> SAVANNA_RUINS = new ArrayList<>();
    public static List<BasePiece> SNOWY_RUINS = new ArrayList<>();
    public static List<BasePiece> TAIGA_RUINS = new ArrayList<>();

    public static void init() {
        BADLANDS_POOL = registerPool("ruins/badlands/starts", BADLANDS_RUINS);
        DESERT_POOL = registerPool("ruins/desert/starts", DESERT_RUINS);
        FOREST_POOL = registerPool("ruins/forest/starts", FOREST_RUINS);
        JUNGLE_POOL = registerPool("ruins/jungle/starts", JUNGLE_RUINS);
        MOUNTAINS_POOL = registerPool("ruins/mountains/starts", MOUNTAINS_RUINS);
        NETHER_POOL = registerPool("ruins/nether/starts", NETHER_RUINS);
        PLAINS_POOL = registerPool("ruins/plains/starts", PLAINS_RUINS);
        SAVANNA_POOL = registerPool("ruins/savanna/starts", SAVANNA_RUINS);
        SNOWY_POOL = registerPool("ruins/snowy/starts", SNOWY_RUINS);
        TAIGA_POOL = registerPool("ruins/taiga/starts", TAIGA_RUINS);
    }

    @Nullable
    private static StructurePool registerPool(String startPool, List<BasePiece> pieces) {
        if (pieces.isEmpty())
            return emptyPool(startPool);

        // this is populated with starts for each custom ruin
        List<Pair<Function<StructurePool.Projection, ? extends StructurePoolElement>, Integer>> starts = new ArrayList<>();

        // iterate over each custom ruin, get all the start pools, and put them into the starts list
        pieces.forEach(piece -> starts.addAll(piece.getStarts()));

        // return the start pool containing all the custom ruin starts
        return StructurePools.register(
            new StructurePool(
                new Identifier(Strange.MOD_ID, startPool),
                new Identifier("empty"),
                ImmutableList.copyOf(starts),
                StructurePool.Projection.RIGID
            )
        );
    }

    private static StructurePool emptyPool(String poolName) {
        return new StructurePool(
            new Identifier(Strange.MOD_ID, poolName),
            new Identifier("empty"),
            ImmutableList.of(Pair.of(StructurePoolElement.method_30438(), 1)),
            StructurePool.Projection.RIGID
        );
    }
}
