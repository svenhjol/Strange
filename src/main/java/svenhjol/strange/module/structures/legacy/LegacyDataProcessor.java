package svenhjol.strange.module.structures.legacy;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.jetbrains.annotations.Nullable;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.module.structures.Processors;

import java.util.Random;

public class LegacyDataProcessor extends StructureProcessor {
    public static final LegacyDataProcessor INSTANCE = new LegacyDataProcessor();
    public static final Codec<LegacyDataProcessor> CODEC = Codec.unit(() -> INSTANCE);
    public final StrangeDataResolver RESOLVER = new StrangeDataResolver();

    @Nullable
    @Override
    public StructureBlockInfo processBlock(LevelReader level, BlockPos pos, BlockPos pos2, StructureBlockInfo UNUSED, StructureBlockInfo blockInfo, StructurePlaceSettings placement) {
        Block block = blockInfo.state.getBlock();
        CompoundTag nbt = blockInfo.nbt;
        if (nbt != null && !nbt.isEmpty()) {
            LogHelper.debug(getClass(), nbt.getAsString());
        }
        if (block == Blocks.STRUCTURE_BLOCK) {
            StructureMode mode = StructureMode.valueOf(blockInfo.nbt.getString("mode"));
            if (mode == StructureMode.DATA) {
                Rotation rotation = placement.getRotation();
                Random posRandom = new Random(pos.asLong());
                return RESOLVER.replace(level, rotation, blockInfo, posRandom);
            }
        }

        return blockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Processors.LEGACY;
    }

    public static class StrangeDataResolver extends LegacyDataResolver {
        private String data;
        private Rotation rotation;
        private BlockState state;
        private BlockPos pos;
        private LevelReader level;
        private CompoundTag tag;
        private Random fixedRandom; // fixed according to parent template
        private Random random; // random according to the replaced block hashcode
        private float chance;

        public StructureBlockInfo replace(LevelReader level, Rotation rotation, StructureBlockInfo blockInfo, Random random) {
            this.level = level;
            this.fixedRandom = random;
            this.rotation = rotation;
            this.pos = blockInfo.pos;
            this.state = null;
            this.tag = null;
            this.random = new Random(blockInfo.hashCode());

            // pipe character acts as an OR. Data will use one of the definitions at random.
            String data = blockInfo.nbt.getString("metadata");
            if (data.contains("|")) {
                String[] split = data.split("\\|");
                data = split[this.random.nextInt(split.length)];
            }

            this.data = data.trim();
            this.chance = getChance(this.data, 0.0F);

            this.state = Blocks.LAPIS_BLOCK.defaultBlockState();
            return new StructureBlockInfo(pos, state, tag);
        }

        public float getChance(String data, float fallback) {
            int i = getValue("chance", data, 0);
            return i == 0 ? fallback : ((float) i) / 100.0F;
        }

        public boolean withChance(float chance) {
            float f = random.nextFloat();
            return this.chance > 0 ? f < this.chance : f < chance;
        }
    }
}
