package svenhjol.strange.module.structures.processor;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.module.structures.Processors;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GravelifyProcessor extends StructureProcessor {
    public static final Codec<GravelifyProcessor> CODEC = Codec.FLOAT.fieldOf("amount").orElse(1.0F).xmap(GravelifyProcessor::new, p -> p.amount).codec();
    public static List<Block> REPLACEABLE;
    private final float amount;

    public GravelifyProcessor(float amount) {
        this.amount = amount;
    }

    @Nullable
    @Override
    public StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureBlockInfo NEVER, StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        Random random = structurePlaceSettings.getRandom(structureBlockInfo2.pos);
        BlockPos pos = structureBlockInfo2.pos;
        BlockState state = structureBlockInfo2.state;

        if (REPLACEABLE.contains(state.getBlock()) && random.nextFloat() < amount) {
            return new StructureBlockInfo(pos, Blocks.GRAVEL.defaultBlockState(), null);
        }

        return structureBlockInfo2;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Processors.GRAVELIFY;
    }

    static {
        REPLACEABLE = Arrays.asList(
            Blocks.STONE_BRICKS,
            Blocks.CRACKED_STONE_BRICKS,
            Blocks.MOSSY_STONE_BRICKS,
            Blocks.ANDESITE,
            Blocks.COBBLESTONE,
            Blocks.MOSSY_COBBLESTONE
        );
    }
}
