package svenhjol.strange.module.structures.processor;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import svenhjol.strange.module.structures.Processors;

import java.util.Arrays;
import java.util.List;

public class PreciousStoneOreProcessor extends BaseOreStructureProcessor {
    public static final Codec<PreciousStoneOreProcessor> CODEC = Codec.FLOAT.fieldOf("chance").orElse(1.0F).xmap(PreciousStoneOreProcessor::new, p -> p.chance).codec();
    public static List<BlockState> ORE_REPLACEMENTS;

    public PreciousStoneOreProcessor(float chance) {
        this.chance = chance;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Processors.PRECIOUS_STONE_ORE;
    }

    @Override
    protected boolean isOre(Block block) {
        return block == Blocks.DIAMOND_ORE;
    }

    @Override
    protected List<BlockState> getOreReplacements() {
        return ORE_REPLACEMENTS;
    }

    static {
        ORE_REPLACEMENTS = Arrays.asList(
            Blocks.GOLD_ORE.defaultBlockState(),
            Blocks.LAPIS_ORE.defaultBlockState(),
            Blocks.EMERALD_ORE.defaultBlockState(),
            Blocks.DIAMOND_ORE.defaultBlockState()
        );
    }
}
