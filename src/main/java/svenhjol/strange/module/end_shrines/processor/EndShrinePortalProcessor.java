package svenhjol.strange.module.end_shrines.processor;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import svenhjol.strange.module.end_shrines.EndShrines;

import javax.annotation.Nullable;

/**
 * Responsible for adding the dimension to the End Shrine portal block.
 * The dimension is selected randomly from EndShrines.DESTINATIONS.
 * Specify a dimension manually with the "type" argument.
 */
public class EndShrinePortalProcessor extends StructureProcessor {
    public static final Codec<EndShrinePortalProcessor> CODEC = Codec.STRING.fieldOf("type").orElse("").xmap(EndShrinePortalProcessor::new, p -> p.type).codec();

    private final String type;

    public EndShrinePortalProcessor(String type) {
        this.type = type;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureTemplate.StructureBlockInfo NOPE, StructureTemplate.StructureBlockInfo blockInfo, StructurePlaceSettings structurePlaceSettings) {
        ResourceLocation dimension = null;
        var random = structurePlaceSettings.getRandom(blockPos);
        var pos = blockInfo.pos;
        var state = blockInfo.state;
        var block = state.getBlock();

        if (block != EndShrines.END_SHRINE_PORTAL_BLOCK) {
            return blockInfo;
        }

        if (type.isEmpty()) {
            var id = new ResourceLocation(type);
            if (EndShrines.DESTINATIONS.contains(id)) {
                dimension = id;
            }
        }

        if (dimension == null) {
            dimension = EndShrines.DESTINATIONS.get(random.nextInt(EndShrines.DESTINATIONS.size()));
        }

        var nbt = new CompoundTag();

        var container = EndShrines.END_SHRINE_PORTAL_BLOCK_ENTITY.create(BlockPos.ZERO, EndShrines.END_SHRINE_PORTAL_BLOCK.defaultBlockState());
        if (container != null) {
            container.dimension = dimension;
            nbt = container.saveWithFullMetadata();
        }

        return new StructureTemplate.StructureBlockInfo(pos, state, nbt);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return EndShrines.END_SHRINE_PORTAL_PROCESSOR;
    }
}
