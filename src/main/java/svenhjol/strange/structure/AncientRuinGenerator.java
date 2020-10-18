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

public class AncientRuinGenerator {
    public static StructurePool OVERWORLD_POOL;

    public static List<BasePiece> OVERWORLD_RUINS = new ArrayList<>();

    public static void init() {
        OVERWORLD_POOL = registerPool("ancient_ruins/overworld/starts", OVERWORLD_RUINS);
    }

    @Nullable
    private static StructurePool registerPool(String startPool, List<BasePiece> pieces) {
        if (pieces.isEmpty())
            return emptyPool(startPool);

        // this is populated with starts for each custom ruin
        List<Pair<Function<StructurePool.Projection, ? extends StructurePoolElement>, Integer>> starts = new ArrayList<>();

        // iterate over each custom ruin, get all the start pools, and put them into the starts list
        pieces.forEach(ruin -> starts.addAll(ruin.getStarts()));

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
