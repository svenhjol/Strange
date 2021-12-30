package svenhjol.strange.module.structures.processor;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.module.structures.DataBlock;
import svenhjol.strange.module.structures.Processors;
import svenhjol.strange.module.structures.Structures;

import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

public class DataBlockProcessor extends StructureProcessor {
    public static final DataBlockProcessor INSTANCE = new DataBlockProcessor();
    public static final Codec<DataBlockProcessor> CODEC = Codec.unit(() -> INSTANCE);

    private Random random;
    private CompoundTag nbt;
    private BlockState state;
    private Block block;
    private BlockPos pos;

    @Nullable
    @Override
    public StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureBlockInfo NEVER, StructureBlockInfo blockInfo, StructurePlaceSettings structurePlaceSettings) {
        random = structurePlaceSettings.getRandom(blockInfo.pos);
        state = blockInfo.state;
        nbt = blockInfo.nbt;
        pos = blockInfo.pos;
        block = state.getBlock();

        if (block != Structures.DATA_BLOCK) {
            return blockInfo;
        }

        // Pipe character acts as an OR. Data will use one of the definitions at random.
        var metadata = nbt.getString("metadata").toLowerCase(Locale.ROOT);
        if (metadata.contains("|")) {
            var strings = Arrays.stream(metadata.split("\\|")).map(String::trim).toList();
            metadata = strings.get(random.nextInt(strings.size()));
        }

        if (metadata.startsWith("chest")) {
            return processChest();
        }

        if (metadata.startsWith("spawner")) {
            return new StructureBlockInfo(pos, Blocks.SPAWNER.defaultBlockState(), null);
        }

        return getAir(pos);
    }

    private StructureBlockInfo processChest() {
        ResourceLocation loot = null;
        var metadata = nbt.getString("metadata");
        var lootValue = Processors.getValue(metadata, "loot", "");
        var blockChance = Processors.getValue(metadata, "chance", "");
        var waterlogged = Processors.getValue(metadata, "waterlogged", "false");
        double newChance;

        if (!blockChance.isEmpty()) {
            newChance = Integer.parseInt(blockChance) / 100D;
        } else {
            newChance = 1.0D;
        }

        if (random.nextDouble() > newChance) {
            return getAir(pos);
        }

        var newState = Blocks.CHEST.defaultBlockState()
            .setValue(ChestBlock.FACING, state.getValue(DataBlock.FACING));

        if (Boolean.parseBoolean(waterlogged)) {
            newState = newState.setValue(ChestBlock.WATERLOGGED, true);
        }

        if (!lootValue.isEmpty()) {
            loot = new ResourceLocation(lootValue);
        }

        return new StructureBlockInfo(pos, newState, ChestLootProcessor.createContainerNbt(random, loot));
    }

    private StructureBlockInfo getAir(BlockPos pos) {
        return new StructureBlockInfo(pos, Blocks.AIR.defaultBlockState(), null);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Processors.DATA;
    }
}
