package svenhjol.strange.module.structures.processor;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.module.structures.Processors;

import java.util.Random;

public class SmithingDecorationProcessor extends StructureProcessor {
    public static final Codec<SmithingDecorationProcessor> CODEC = Codec.FLOAT.fieldOf("chance").orElse(1.0F).xmap(SmithingDecorationProcessor::new, p -> p.chance).codec();
    private final float chance;

    public SmithingDecorationProcessor(float chance) {
        this.chance = chance;
    }

    @Nullable
    @Override
    public StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureBlockInfo NONONONO, StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        Random random = structurePlaceSettings.getRandom(structureBlockInfo2.pos);
        BlockPos pos = structureBlockInfo2.pos;
        BlockState state = structureBlockInfo2.state;
        CompoundTag nbt = structureBlockInfo2.nbt;
        BlockState newState;

        if (!(state.getBlock() instanceof AnvilBlock)) {
            return structureBlockInfo2;
        }

        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);

        if (random.nextFloat() < chance) {
            if (random.nextBoolean()) {
                newState = Blocks.DAMAGED_ANVIL.defaultBlockState();
            } else {
                newState = Blocks.CHIPPED_ANVIL.defaultBlockState();
            }
        } else {
            return structureBlockInfo2;
        }

        newState = newState.setValue(HorizontalDirectionalBlock.FACING, facing);
        return new StructureBlockInfo(pos, newState, nbt);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Processors.ANVIL_DAMAGE;
    }
}
