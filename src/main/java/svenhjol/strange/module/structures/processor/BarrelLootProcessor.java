package svenhjol.strange.module.structures.processor;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.module.structures.Processors;

import java.util.Random;

public class BarrelLootProcessor extends StructureProcessor {
    public static final Codec<BarrelLootProcessor> CODEC = Codec.FLOAT.fieldOf("chance").orElse(1.0F).xmap(BarrelLootProcessor::new, p -> p.chance).codec();
    public static final String FALLBACK_LOOT_TABLE = "chests/village/village_fisher";

    private final float chance;
    private Random random;

    public BarrelLootProcessor(float chance) {
        this.chance = chance;
    }

    @Nullable
    @Override
    public StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureBlockInfo OMGNO, StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        random = structurePlaceSettings.getRandom(structureBlockInfo2.pos);
        BlockState state = structureBlockInfo2.state;
        BlockPos pos = structureBlockInfo2.pos;
        Block block = state.getBlock();
        StructureBlockInfo out;

        if (block instanceof BarrelBlock) {
            out = processBarrel(structureBlockInfo2);
        } else {
            return structureBlockInfo2;
        }

        return out == null ? new StructureBlockInfo(pos, Blocks.AIR.defaultBlockState(), null) : out;
    }

    @Nullable
    private StructureBlockInfo processBarrel(StructureBlockInfo blockInfo) {
        if (random.nextFloat() > chance) return null;

        String loot;
        BlockState state = blockInfo.state;
        BlockState newState = Blocks.BARREL.defaultBlockState()
            .setValue(BarrelBlock.FACING, state.getValue(BarrelBlock.FACING));

        if (blockInfo.nbt != null) {
            loot = blockInfo.nbt.getString(RandomizableContainerBlockEntity.LOOT_TABLE_TAG);
            if (loot.isEmpty()) {
                loot = FALLBACK_LOOT_TABLE;
            }
        } else {
            loot = FALLBACK_LOOT_TABLE;
        }

        return new StructureBlockInfo(blockInfo.pos, newState, createContainerNbt(new ResourceLocation(loot)));
    }

    private CompoundTag createContainerNbt(ResourceLocation lootTable) {
        CompoundTag nbt = new CompoundTag();

        BarrelBlockEntity container = BlockEntityType.BARREL.create(BlockPos.ZERO, Blocks.BARREL.defaultBlockState());
        if (container != null) {
            container.setLootTable(lootTable, random.nextLong());
            nbt = container.saveWithFullMetadata();
        }

        return nbt;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Processors.BARREL_LOOT;
    }
}
