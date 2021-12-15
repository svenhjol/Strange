package svenhjol.strange.module.structures.processor;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.module.structures.EntityBlock;
import svenhjol.strange.module.structures.EntityBlockEntity;
import svenhjol.strange.module.structures.Processors;
import svenhjol.strange.module.structures.Structures;

import java.util.Random;

public class EntityBlockProcessor extends StructureProcessor {
    public static final Codec<EntityBlockProcessor> CODEC = Codec.FLOAT.fieldOf("chance").orElse(1.0F).xmap(EntityBlockProcessor::new, p -> p.chance).codec();
    private final float chance;

    public EntityBlockProcessor(float chance) {
        this.chance = chance;
    }

    @Nullable
    @Override
    public StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureBlockInfo NONONONO, StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        Random random = structurePlaceSettings.getRandom(structureBlockInfo2.pos);
        BlockState state = structureBlockInfo2.state;
        BlockPos pos = structureBlockInfo2.pos;
        CompoundTag nbt = structureBlockInfo2.nbt;
        Block block = state.getBlock();

        if (block != Structures.ENTITY_BLOCK) {
            return structureBlockInfo2;
        }

        if (random.nextFloat() < chance) {
            EntityBlockEntity entityBlock = Structures.ENTITY_BLOCK_ENTITY.create(BlockPos.ZERO, Structures.ENTITY_BLOCK.defaultBlockState());
            if (entityBlock != null) {
                nbt.putBoolean(EntityBlockEntity.PRIMED_TAG, true);
                BlockState newState = state.setValue(EntityBlock.HIDDEN, true);
                return new StructureBlockInfo(pos, newState, nbt);
            }
        }

        return new StructureBlockInfo(pos, Blocks.AIR.defaultBlockState(), null);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Processors.ENTITY;
    }
}
