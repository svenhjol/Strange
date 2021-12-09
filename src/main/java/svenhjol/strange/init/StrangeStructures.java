package svenhjol.strange.init;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.structure.StrangeStructureProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StrangeStructures {
    public static final StrangeStructureProcessor STRUCTURE_PROCESSOR;
    public static final Codec<StrangeStructureProcessor> CODEC;
    public static final StructureProcessorType<StrangeStructureProcessor> STRUCTURE_PROCESSOR_TYPE;
    public static final List<StructureProcessor> PROCESSORS;

    public static void init() {
        PROCESSORS.addAll(Arrays.asList(
            new BlockIgnoreProcessor(ImmutableList.of(Blocks.GRAY_STAINED_GLASS)),
            STRUCTURE_PROCESSOR
        ));

        CommonRegistry.structureProcessor(new ResourceLocation(Strange.MOD_ID, "structure_processor"), STRUCTURE_PROCESSOR_TYPE);
    }

    static {
        STRUCTURE_PROCESSOR = new StrangeStructureProcessor();
        CODEC = Codec.unit(STRUCTURE_PROCESSOR);
        STRUCTURE_PROCESSOR_TYPE = () -> CODEC;
        PROCESSORS = new ArrayList<>();
    }
}
