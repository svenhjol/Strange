package svenhjol.strange.feature.stone_circles.common;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import svenhjol.charm.charmony.feature.RegisterHolder;
import svenhjol.strange.feature.stone_circles.StoneCircles;

import java.util.function.Supplier;

public final class Registers extends RegisterHolder<StoneCircles> {
    private static final String PIECE_ID = "stone_circle_piece";

    public final Supplier<StructureType<StoneCircleStructure>> structureType;
    public final Supplier<StructurePieceType> structurePiece;

    public Registers(StoneCircles feature) {
        super(feature);
        var registry = feature.registry();

        structureType = registry.structure(StoneCircles.STRUCTURE_ID, () -> StoneCircleStructure.CODEC);
        structurePiece = registry.structurePiece(PIECE_ID, () -> StoneCirclePiece::new);
    }

    @Override
    public void onEnabled() {
        var registry = feature().registry();
        
        registry.villagerTrade(() -> VillagerProfession.CARTOGRAPHER, 2,
            () -> new VillagerTrades.TreasureMapForEmeralds(
                7, // emerald cost
                Tags.ON_STONE_CIRCLE_MAPS,
                "filled_map.strange.stone_circle", 
                MapDecorationTypes.TARGET_X, 
                12, // max trades
                5) // XP for villager
        );
    }
}
