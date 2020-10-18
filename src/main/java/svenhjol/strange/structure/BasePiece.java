package svenhjol.strange.structure;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.processor.StructureProcessorLists;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class BasePiece {
    private final String modId;
    private final String subFolder;
    private final String ruinName;
    private final List<Pair<Function<StructurePool.Projection, ? extends StructurePoolElement>, Integer>> starts = new ArrayList<>();

    public BasePiece(String modId, String subFolder, String ruinName) {
        this.modId = modId;
        this.subFolder = subFolder;
        this.ruinName = ruinName;
    }

    public List<Pair<Function<StructurePool.Projection, ? extends StructurePoolElement>, Integer>> getStarts() {
        return starts;
    }

    protected void addStart(String pieceName, int weight) {
        starts.add(Pair.of(StructurePoolElement.method_30435(getPiecePath(pieceName), StructureProcessorLists.EMPTY), weight));
    }

    protected void registerPool(String poolName, Map<String, Integer> elements) {
        final List<Pair<Function<StructurePool.Projection, ? extends StructurePoolElement>, Integer>> pieces = new ArrayList<>();

        elements.forEach((piece, weight) ->
            pieces.add(Pair.of(StructurePoolElement.method_30435(getPiecePath(piece), StructureProcessorLists.EMPTY), weight)));

        StructurePools.register(new StructurePool(
            getPoolPath(poolName),
            getPoolPath("ends"),
            ImmutableList.copyOf(pieces),
            StructurePool.Projection.RIGID
        ));
    }

    protected String getPiecePath(String piece) {
        return modId + ":" + subFolder + "/" + ruinName + "/" + piece;
    }

    protected Identifier getPoolPath(String pool) {
        return new Identifier(modId, subFolder + "/" + ruinName + "/" + pool);
    }
}
