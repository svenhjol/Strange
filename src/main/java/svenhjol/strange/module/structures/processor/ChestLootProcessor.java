package svenhjol.strange.module.structures.processor;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.module.structures.DataBlock;
import svenhjol.strange.module.structures.Processors;

import java.util.Random;

public class ChestLootProcessor extends StructureProcessor {
    public static final Codec<ChestLootProcessor> CODEC = Codec.FLOAT.fieldOf("chance").orElse(1.0F).xmap(ChestLootProcessor::new, p -> p.chance).codec();
    public static final String FALLBACK_LOOT_TABLE = "chests/simple_dungeon";

    private final float chance;
    private Random random;

    public ChestLootProcessor(float chance) {
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

        if (block instanceof ChestBlock) {
            out = processChest(structureBlockInfo2);
        } else if (block instanceof DataBlock) {
            out = processDataBlock(structureBlockInfo2);
        } else {
            return structureBlockInfo2;
        }

        return out == null ? new StructureBlockInfo(pos, Blocks.AIR.defaultBlockState(), null) : out;
    }

    @Nullable
    private StructureBlockInfo processChest(StructureBlockInfo blockInfo) {
        if (random.nextFloat() < chance) return null;

        String loot;
        BlockState state = blockInfo.state;
        BlockState newState = Blocks.CHEST.defaultBlockState()
            .setValue(ChestBlock.FACING, state.getValue(ChestBlock.FACING));

        if (blockInfo.nbt != null) {
            loot = blockInfo.nbt.getString(RandomizableContainerBlockEntity.LOOT_TABLE_TAG);
        } else {
            loot = FALLBACK_LOOT_TABLE;
        }

        return new StructureBlockInfo(blockInfo.pos, newState, createContainerNbt(new ResourceLocation(loot)));
    }

    @Nullable
    private StructureBlockInfo processDataBlock(StructureBlockInfo blockInfo) {
        if (blockInfo.nbt == null || !blockInfo.nbt.getString("metadata").startsWith("chest")) {
            return blockInfo;
        }

        if (random.nextFloat() > chance) return null;

        BlockState state = blockInfo.state;
        BlockState newState = Blocks.CHEST.defaultBlockState()
            .setValue(ChestBlock.FACING, state.getValue(DataBlock.FACING));

        String metadata = blockInfo.nbt.getString("metadata");
        String loot = Processors.getMetadataValue(metadata, "loot", FALLBACK_LOOT_TABLE);

        return new StructureBlockInfo(blockInfo.pos, newState, createContainerNbt(new ResourceLocation(loot)));
    }

    private CompoundTag createContainerNbt(ResourceLocation lootTable) {
        CompoundTag nbt = new CompoundTag();

        ChestBlockEntity container = BlockEntityType.CHEST.create(BlockPos.ZERO, Blocks.CHEST.defaultBlockState());
        if (container != null) {
            container.setLootTable(lootTable, random.nextLong());
            nbt = container.saveWithFullMetadata();
        }

        return nbt;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Processors.CHEST_LOOT;
    }
}
