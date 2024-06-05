package svenhjol.strange.feature.stone_circles.common;

import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import svenhjol.charm.charmony.feature.RegisterHolder;
import svenhjol.strange.feature.stone_circles.StoneCircles;

import java.util.function.Supplier;

public final class Registers extends RegisterHolder<StoneCircles> {
    private static final String STRUCTURE_ID = "stone_circle";
    private static final String PIECE_ID = "stone_circle_piece";

    public final Supplier<StructureType<StoneCircleStructure>> structureType;
    public final Supplier<StructurePieceType> structurePiece;

    public Registers(StoneCircles feature) {
        super(feature);
        var registry = feature.registry();

        structureType = registry.structure(STRUCTURE_ID, () -> StoneCircleStructure.CODEC);
        structurePiece = registry.structurePiece(PIECE_ID, () -> StoneCirclePiece::new);
    }
}
