package svenhjol.strange.module.structures.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.module.structures.DataBlock;
import svenhjol.strange.module.structures.Processors;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ChestLootProcessor extends StructureProcessor {
    public static final Codec<ChestLootProcessor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.FLOAT.fieldOf("chance").orElse(1.0F).forGetter(p -> p.chance),
        Codec.STRING.fieldOf("match_loot").orElse("").forGetter(p -> p.matchLoot),
        Codec.BOOL.fieldOf("remove").orElse(true).forGetter(p -> p.remove)
    ).apply(instance, ChestLootProcessor::new));

    public static List<ResourceLocation> TABLES;

    private final float chance; // Chance (out of 1.0) of a matching chest being processed.
    private final String matchLoot; // If set, only chests that match this loot table will be processed. Otherwise, this processor will be skipped.
    private final boolean remove; // If true and a matching chest fails the chance test, the chest will be replaced with air.
    private Random random;

    public ChestLootProcessor(float chance, String matchLoot, boolean remove) {
        this.chance = chance;
        this.matchLoot = matchLoot;
        this.remove = remove;
    }

    @Nullable
    @Override
    public StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureBlockInfo OMGNO, StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        random = structurePlaceSettings.getRandom(structureBlockInfo2.pos);
        BlockState state = structureBlockInfo2.state;
        Block block = state.getBlock();

        if (block instanceof ChestBlock) {
            return processChest(structureBlockInfo2);
        } else if (block instanceof DataBlock) {
            return processDataBlock(structureBlockInfo2);
        } else {
            return structureBlockInfo2;
        }
    }

    private StructureBlockInfo processChest(StructureBlockInfo blockInfo) {
        ResourceLocation loot;
        BlockState state = blockInfo.state;
        BlockState newState = Blocks.CHEST.defaultBlockState()
            .setValue(ChestBlock.FACING, state.getValue(ChestBlock.FACING));

        if (blockInfo.nbt != null) {
            String lootValue = blockInfo.nbt.getString(RandomizableContainerBlockEntity.LOOT_TABLE_TAG);

            if (!matchLoot.isEmpty() && !lootValue.equalsIgnoreCase(matchLoot)) {
                return blockInfo;
            }

            loot = new ResourceLocation(lootValue);

        } else {

            loot = TABLES.get(random.nextInt(TABLES.size()));

        }

        if (random.nextFloat() < chance) {
            return new StructureBlockInfo(blockInfo.pos, Blocks.AIR.defaultBlockState(), null);
        }

        return new StructureBlockInfo(blockInfo.pos, newState, createContainerNbt(loot));
    }

    private StructureBlockInfo processDataBlock(StructureBlockInfo blockInfo) {
        if (blockInfo.nbt == null || !blockInfo.nbt.getString("metadata").startsWith("chest")) {
            return blockInfo;
        }

        ResourceLocation loot;
        String metadata = blockInfo.nbt.getString("metadata");
        String lootValue = Processors.getValue(metadata, "loot", "");
        String blockChance = Processors.getValue(metadata, "chance", String.valueOf(chance));
        double blockChanceValue = Double.parseDouble(blockChance);

        if (!matchLoot.isEmpty() && !lootValue.equalsIgnoreCase(matchLoot)) {
            return blockInfo;
        }

        if (random.nextDouble() > blockChanceValue) {
            return new StructureBlockInfo(blockInfo.pos, Blocks.AIR.defaultBlockState(), null);
        }

        BlockState state = blockInfo.state;
        BlockState newState = Blocks.CHEST.defaultBlockState()
            .setValue(ChestBlock.FACING, state.getValue(DataBlock.FACING));

        if (!lootValue.isEmpty()) {
            loot = new ResourceLocation(lootValue);
        } else {
            loot = TABLES.get(random.nextInt(TABLES.size()));
        }

        return new StructureBlockInfo(blockInfo.pos, newState, createContainerNbt(loot));
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

    static {
        TABLES = Arrays.asList(
            BuiltInLootTables.SIMPLE_DUNGEON,
            BuiltInLootTables.ABANDONED_MINESHAFT,
            BuiltInLootTables.DESERT_PYRAMID,
            BuiltInLootTables.JUNGLE_TEMPLE,
            BuiltInLootTables.END_CITY_TREASURE
        );
    }
}
