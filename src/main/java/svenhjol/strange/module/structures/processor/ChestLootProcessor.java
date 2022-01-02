package svenhjol.strange.module.structures.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import svenhjol.strange.module.structures.Processors;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ChestLootProcessor extends StructureProcessor {
    public static final double DEFAULT_CHANCE = 0.75D;

    public static final Codec<ChestLootProcessor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.DOUBLE.fieldOf("chance").orElse(DEFAULT_CHANCE).forGetter(p -> p.chance),
        Codec.STRING.fieldOf("loot").orElse("").forGetter(p -> p.loot),
        Codec.BOOL.fieldOf("remove").orElse(true).forGetter(p -> p.remove)
    ).apply(instance, ChestLootProcessor::new));

    public static List<ResourceLocation> TABLES;

    private final double chance; // Chance (out of 1.0) of a matching chest being processed.
    private final String loot; // Optional manually defined loot table for all chests in this structure piece that don't have a loot table.
    private final boolean remove; // If true and a matching chest fails the chance test, the chest will be replaced with air.

    public ChestLootProcessor(double chance, String loot, boolean remove) {
        this.chance = chance;
        this.loot = loot;
        this.remove = remove;
    }

    @Nullable
    @Override
    public StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureBlockInfo OMGNO, StructureBlockInfo blockInfo, StructurePlaceSettings structurePlaceSettings) {
        var random = structurePlaceSettings.getRandom(blockInfo.pos);
        var state = blockInfo.state;
        var block = state.getBlock();

        if (!(block instanceof ChestBlock)) {
            return blockInfo;
        }

        String lootValue = "";
        ResourceLocation useLoot;
        var newState = state
            .setValue(ChestBlock.FACING, state.getValue(ChestBlock.FACING));

        if (blockInfo.nbt != null) {
            lootValue = blockInfo.nbt.getString(RandomizableContainerBlockEntity.LOOT_TABLE_TAG);
        }

        if (lootValue.isEmpty()) {
            if (!loot.isEmpty()) {
                useLoot = new ResourceLocation(loot);
            } else {
                useLoot = TABLES.get(random.nextInt(TABLES.size()));
            }
        } else {
            useLoot = new ResourceLocation(lootValue);
        }

        if (random.nextDouble() > chance) {
            return getAir(blockInfo.pos);
        }

        return new StructureBlockInfo(blockInfo.pos, newState, createContainerNbt(random, useLoot));
    }

    public static CompoundTag createContainerNbt(Random random, @Nullable ResourceLocation lootTable) {
        var nbt = new CompoundTag();

        var container = BlockEntityType.CHEST.create(BlockPos.ZERO, Blocks.CHEST.defaultBlockState());
        if (container != null) {
            if (lootTable != null) {
                container.setLootTable(lootTable, random.nextLong());
            }

            nbt = container.saveWithFullMetadata();
        }

        return nbt;
    }

    private StructureBlockInfo getAir(BlockPos pos) {
        return new StructureBlockInfo(pos, Blocks.AIR.defaultBlockState(), null);
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
