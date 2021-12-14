package svenhjol.strange.module.structures.processor;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.module.structures.Processors;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class StoneBricksDecayProcessor extends StructureProcessor {
    public static final StoneBricksDecayProcessor INSTANCE = new StoneBricksDecayProcessor();
    public static final Codec<StoneBricksDecayProcessor> CODEC = Codec.unit(() -> INSTANCE);
    public static List<BlockState> REPLACEMENTS;

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureTemplate.StructureBlockInfo NOOOOO, StructureTemplate.StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        Random random = structurePlaceSettings.getRandom(structureBlockInfo2.pos);
        BlockPos pos = structureBlockInfo2.pos;
        BlockState state = structureBlockInfo2.state;
        CompoundTag nbt = structureBlockInfo2.nbt;

        if (!(state.getBlock() == Blocks.STONE_BRICKS)) {
            return structureBlockInfo2;
        }

        BlockState newState = REPLACEMENTS.get(random.nextInt(REPLACEMENTS.size()));
        return new StructureTemplate.StructureBlockInfo(pos, newState, nbt);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Processors.STONE_BRICKS_DECAY;
    }

    static {
        REPLACEMENTS = Arrays.asList(
            Blocks.STONE_BRICKS.defaultBlockState(),
            Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),
            Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
            Blocks.ANDESITE.defaultBlockState(),
            Blocks.COBBLESTONE.defaultBlockState(),
            Blocks.MOSSY_COBBLESTONE.defaultBlockState()
        );
    }
}
