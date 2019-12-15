package svenhjol.strange.ruins.feature;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import svenhjol.strange.Strange;

import javax.annotation.Nullable;

public class AirBlockProcessor extends StructureProcessor
{
    private static final IStructureProcessorType TYPE = Registry.register(Registry.STRUCTURE_PROCESSOR, Strange.MOD_ID + ":air_block", AirBlockProcessor::new);

    public AirBlockProcessor()
    {
        // no op
    }

    public AirBlockProcessor(Dynamic<?> dynamic)
    {
        this();
    }

    @Nullable
    @Override
    public BlockInfo process(IWorldReader world, BlockPos pos, BlockInfo unused, BlockInfo blockInfo, PlacementSettings placement, @Nullable Template template)
    {
        if (blockInfo.state.getBlock() == Blocks.AIR) {
            if (blockInfo.pos == null) return blockInfo;

//            Biome biome = world.getBiome(blockInfo.pos);
            if (world.getBlockState(blockInfo.pos.up()).getMaterial().isLiquid())
                return new BlockInfo(blockInfo.pos, Blocks.WATER.getDefaultState(), null);

            if (pos.getY() < world.getSeaLevel())
                return new BlockInfo(blockInfo.pos, Blocks.CAVE_AIR.getDefaultState(), null);
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
