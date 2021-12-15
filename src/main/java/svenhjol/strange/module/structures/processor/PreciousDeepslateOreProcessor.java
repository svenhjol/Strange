package svenhjol.strange.module.structures.processor;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import svenhjol.strange.module.structures.Processors;

import java.util.Arrays;
import java.util.List;

public class PreciousDeepslateOreProcessor extends BaseOreStructureProcessor {
    public static final Codec<PreciousDeepslateOreProcessor> CODEC = Codec.FLOAT.fieldOf("chance").orElse(1.0F).xmap(PreciousDeepslateOreProcessor::new, p -> p.chance).codec();
    public static List<BlockState> ORE_REPLACEMENTS;

    public PreciousDeepslateOreProcessor(float chance) {
        this.chance = chance;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Processors.PRECIOUS_DEEPSLATE_ORE;
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
            Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState(),
            Blocks.DEEPSLATE_LAPIS_ORE.defaultBlockState(),
            Blocks.DEEPSLATE_EMERALD_ORE.defaultBlockState(),
            Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState()
        );
    }
}
