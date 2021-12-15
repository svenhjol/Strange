package svenhjol.strange.module.structures.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.module.structures.Processors;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class SpawnerProcessor extends StructureProcessor {
    public static final Codec<SpawnerProcessor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("type").orElse("").forGetter(p -> p.type),
        Codec.FLOAT.fieldOf("chance").orElse(1.0F).forGetter(p -> p.chance)
    ).apply(instance, SpawnerProcessor::new));

    public static List<String> DEFAULT_TYPES;

    private final String type;
    private final float chance;

    public SpawnerProcessor(String type, float chance) {
        this.type = type;
        this.chance = chance;
    }

    @Nullable
    @Override
    public StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureBlockInfo NEVER, StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        Random random = structurePlaceSettings.getRandom(structureBlockInfo2.pos);
        BlockPos pos = structureBlockInfo2.pos;
        BlockState state = structureBlockInfo2.state;

        if (!(state.getBlock() instanceof SpawnerBlock)) {
            return structureBlockInfo2;
        }

        if (random.nextFloat() < chance) {
            CompoundTag nbt = null;
            ResourceLocation id;

            if (type.isEmpty()) {
                id = new ResourceLocation(DEFAULT_TYPES.get(random.nextInt(DEFAULT_TYPES.size())));
            } else {
                id = new ResourceLocation(type);
            }

            Optional<EntityType<?>> opt = Registry.ENTITY_TYPE.getOptional(id);
            if (opt.isPresent()) {
                SpawnerBlockEntity spawner = BlockEntityType.MOB_SPAWNER.create(BlockPos.ZERO, Blocks.SPAWNER.defaultBlockState());
                if (spawner != null) {
                    spawner.getSpawner().setEntityId(opt.get());
                    nbt = spawner.saveWithFullMetadata();
                }
            }

            if (nbt != null) {
                return new StructureBlockInfo(pos, state, nbt);
            }
        }

        return new StructureBlockInfo(pos, Blocks.AIR.defaultBlockState(), null);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Processors.SPAWNER;
    }

    static {
        DEFAULT_TYPES = Arrays.asList(
            "zombie",
            "skeleton",
            "spider",
            "silverfish"
        );
    }
}
