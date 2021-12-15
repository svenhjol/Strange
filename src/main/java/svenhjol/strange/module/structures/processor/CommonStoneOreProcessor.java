package svenhjol.strange.module.structures.processor;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import svenhjol.strange.module.structures.Processors;

import java.util.Arrays;
import java.util.List;

public class CommonStoneOreProcessor extends BaseOreStructureProcessor {
    public static final Codec<CommonStoneOreProcessor> CODEC = Codec.FLOAT.fieldOf("chance").orElse(1.0F).xmap(CommonStoneOreProcessor::new, p -> p.chance).codec();
    public static List<BlockState> ORE_REPLACEMENTS;

    public CommonStoneOreProcessor(float chance) {
        this.chance = chance;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Processors.COMMON_STONE_ORE;
    }

    @Override
    protected boolean isOre(Block block) {
        return block == Blocks.IRON_ORE;
    }

    @Override
    protected List<BlockState> getOreReplacements() {
        return ORE_REPLACEMENTS;
    }

    static {
        ORE_REPLACEMENTS = Arrays.asList(
            Blocks.IRON_ORE.defaultBlockState(),
            Blocks.COAL_ORE.defaultBlockState(),
            Blocks.REDSTONE_ORE.defaultBlockState(),
            Blocks.COPPER_ORE.defaultBlockState()
        );
    }
}
