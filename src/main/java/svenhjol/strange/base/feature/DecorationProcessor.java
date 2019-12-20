package svenhjol.strange.base.feature;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
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
import svenhjol.strange.base.helper.DecorationHelper;

import javax.annotation.Nullable;
import java.util.Random;

public class DecorationProcessor extends StructureProcessor
{
    private static final IStructureProcessorType TYPE = Registry.register(Registry.STRUCTURE_PROCESSOR, Strange.MOD_ID + ":air_block", DecorationProcessor::new);

    public DecorationProcessor()
    {
        // no op
    }

    public DecorationProcessor(Dynamic<?> dynamic)
    {
        this();
    }

    @Nullable
    @Override
    public BlockInfo process(IWorldReader world, BlockPos pos, BlockInfo unused, BlockInfo blockInfo, PlacementSettings placement, @Nullable Template template)
    {
        // remove air
        if (blockInfo.state.getMaterial() == Material.AIR) {
            if (world.getBlockState(blockInfo.pos.up()).getMaterial().isLiquid())
                return new BlockInfo(blockInfo.pos, Blocks.WATER.getDefaultState(), null);

            if (pos.getY() < world.getSeaLevel())
                return new BlockInfo(blockInfo.pos, Blocks.CAVE_AIR.getDefaultState(), null);
        }

        // remove cave roots - TODO probably isn't working
        if (DecorationHelper.quarkCaveRoots != null
            && blockInfo.state.getBlock() == DecorationHelper.quarkCaveRoots.getRootBlock()
        ) {
            return new BlockInfo(blockInfo.pos, Blocks.CAVE_AIR.getDefaultState(), null);
        }

        // replace structure blocks with decoration
        if (blockInfo.state.getBlock() == Blocks.STRUCTURE_BLOCK) {
            StructureMode mode = StructureMode.valueOf(blockInfo.nbt.getString("mode"));
            if (mode == StructureMode.DATA) {
                return DecorationHelper.STRUCTURE_BLOCK_INSTANCE.replace(placement.getRotation(), blockInfo, blockInfo.nbt.getString("metadata"), new Random(pos.toLong()));
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
