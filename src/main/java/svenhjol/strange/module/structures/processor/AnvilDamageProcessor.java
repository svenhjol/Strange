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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.module.structures.Processors;

import java.util.Random;

public class AnvilDamageProcessor extends StructureProcessor {
    public static final AnvilDamageProcessor INSTANCE = new AnvilDamageProcessor();
    public static final Codec<AnvilDamageProcessor> CODEC = Codec.unit(() -> INSTANCE);

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureTemplate.StructureBlockInfo NONONONO, StructureTemplate.StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        Random random = structurePlaceSettings.getRandom(structureBlockInfo2.pos);
        BlockPos pos = structureBlockInfo2.pos;
        BlockState state = structureBlockInfo2.state;
        CompoundTag nbt = structureBlockInfo2.nbt;
        BlockState newState;

        if (!(state.getBlock() instanceof AnvilBlock)) {
            return structureBlockInfo2;
        }

        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        float f = random.nextFloat();

        if (f < 0.33F) {
            newState = Blocks.DAMAGED_ANVIL.defaultBlockState();
        } else if (f < 0.7F) {
            newState = Blocks.CHIPPED_ANVIL.defaultBlockState();
        } else {
            return structureBlockInfo2;
        }

        newState = newState.setValue(HorizontalDirectionalBlock.FACING, facing);
        return new StructureTemplate.StructureBlockInfo(pos, newState, nbt);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Processors.ANVIL_DAMAGE;
    }
}
