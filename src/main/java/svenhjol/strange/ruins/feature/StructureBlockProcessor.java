package svenhjol.strange.ruins.feature;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.block.StructureBlock;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StructureHelper.StructureBlockReplacement;

import javax.annotation.Nullable;

public class StructureBlockProcessor extends StructureProcessor
{
    private static final IStructureProcessorType TYPE = Registry.register(Registry.STRUCTURE_PROCESSOR, Strange.MOD_ID + ":structure_block", StructureBlockProcessor::new);

    public StructureBlockProcessor()
    {
        // no op
    }

    public StructureBlockProcessor(Dynamic<?> dynamic)
    {
        this();
    }

    @Nullable
    @Override
    public BlockInfo process(IWorldReader world, BlockPos pos, BlockInfo unused, BlockInfo blockInfo, PlacementSettings placement, @Nullable Template template)
    {
        if (blockInfo.state.getBlock() instanceof StructureBlock && blockInfo.nbt != null) {
            StructureBlockReplacement replacement = new StructureBlockReplacement(placement.getRotation(), placement.getRandom(pos));
            StructureMode mode = StructureMode.valueOf(blockInfo.nbt.getString("mode"));
            if (mode == StructureMode.DATA) {
                String metadata = blockInfo.nbt.getString("metadata");
                BlockInfo b = replacement.replace(blockInfo, metadata);
                return b;
            }
        }

        return blockInfo;
    }

    @Override
    protected IStructureProcessorType getType()
    {
        return TYPE;
    }

    @Override
    protected <T> Dynamic<T> serialize0(DynamicOps<T> ops)
    {
        return new Dynamic<>(ops);
    }
}
