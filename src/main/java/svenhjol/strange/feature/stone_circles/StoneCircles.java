package svenhjol.strange.feature.stone_circles;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class StoneCircles extends CommonFeature {
    public static final String STRUCTURE_ID = "stone_circle";
    public static final String PIECE_ID = "stone_circle_piece";
    static final Map<String, IStoneCircleDefinition> DEFINITIONS = new HashMap<>();
    static Codec<IStoneCircleDefinition> definitionsCodec;
    static Supplier<StructureType<StoneCircleStructure>> structureType;
    static Supplier<StructurePieceType> structurePiece;

    @Override
    public void register() {
        var registry = mod().registry();
        StoneCircleDefinitions.init();

        definitionsCodec = StringRepresentable.fromValues(
            () -> DEFINITIONS.values().toArray(new IStoneCircleDefinition[0]));

        structureType = registry.structure(STRUCTURE_ID, StoneCircleStructure.CODEC);
        structurePiece = registry.structurePiece(PIECE_ID, () -> StoneCirclePiece::new);
    }

    public static void registerDefinition(IStoneCircleDefinition definition) {
        var registry = Mods.common(Strange.ID).registry();
        DEFINITIONS.put(definition.name(), definition);
    }
}
