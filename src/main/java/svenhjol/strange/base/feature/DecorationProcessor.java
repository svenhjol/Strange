package svenhjol.strange.base.feature;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RailBlock;
import net.minecraft.block.material.Material;
import net.minecraft.state.properties.RailShape;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeLoader;
import svenhjol.strange.base.helper.DecorationHelper;

import javax.annotation.Nullable;
import java.util.Random;

public class DecorationProcessor extends StructureProcessor
{
    public static final String AIR = "air";
    public static final String WATER = "water";
    public static final String LAVA = "lava";

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
    @SuppressWarnings("ConstantConditions")
    public BlockInfo process(IWorldReader world, BlockPos pos, BlockInfo unused, BlockInfo blockInfo, PlacementSettings placement, @Nullable Template template)
    {
        Rotation rot = placement.getRotation();
        Biome biome = null;

        // it is possible for getBiomes() to be null in certain situations
        if (world != null
            && world.getChunk(pos) != null
            && world.getChunk(pos).getBiomes() != null
            && world.getChunk(pos).getBiomes().length > 0
        ) {
            biome = world.getChunk(pos).getBiome(pos);
        }

        // remove air
        if (blockInfo.state.getMaterial() == Material.AIR) {
            return new BlockInfo(blockInfo.pos, getAirReplacement(template), null);
        }

        // remove cave roots - TODO probably isn't working
        if (StrangeLoader.quarkCaveRoots != null
            && blockInfo.state.getBlock() == StrangeLoader.quarkCaveRoots.getRootBlock()
        ) {
            return new BlockInfo(blockInfo.pos, Blocks.CAVE_AIR.getDefaultState(), null);
        }

        // rotate rails properly
        if (blockInfo.state.getBlock() == Blocks.RAIL) {
            if (world.getBlockState(blockInfo.pos.up()).getMaterial().isLiquid())
                return new BlockInfo(blockInfo.pos, Blocks.CAVE_AIR.getDefaultState(), null);

            BlockState state;

            if (rot == Rotation.CLOCKWISE_90 || rot == Rotation.COUNTERCLOCKWISE_90) {
                state = blockInfo.state.with(RailBlock.SHAPE, RailShape.NORTH_SOUTH);
            } else {
                state = blockInfo.state.with(RailBlock.SHAPE, RailShape.EAST_WEST);
            }
            return new BlockInfo(blockInfo.pos, state, null);
        }

        // replace structure blocks with decoration
        if (blockInfo.state.getBlock() == Blocks.STRUCTURE_BLOCK) {
            StructureMode mode = StructureMode.valueOf(blockInfo.nbt.getString("mode"));
            if (mode == StructureMode.DATA) {
                BlockInfo result = DecorationHelper.STRUCTURE_BLOCK_INSTANCE.replace(placement.getRotation(), blockInfo, blockInfo.nbt.getString("metadata"), new Random(pos.toLong()));
                if (result.state.getMaterial() == Material.AIR) {
                    return new BlockInfo(blockInfo.pos, getAirReplacement(template), null);
                }
                return result;
            }
        }

        return blockInfo;
    }

    protected BlockState getAirReplacement(@Nullable Template template)
    {
        BlockState replace;
        String fill = AIR;

        if (template != null) {
            fill = template.getAuthor(); // TODO hack to get metadata into this method
        }

        switch (fill) {
            case LAVA:
                replace = Blocks.LAVA.getDefaultState();
                break;

            case WATER:
                replace = Blocks.WATER.getDefaultState();
                break;

            case AIR:
            default:
                replace = Blocks.CAVE_AIR.getDefaultState();
                break;
        }

        return replace;
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
