package svenhjol.strange.base;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Blocks;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import svenhjol.strange.helper.StructureHelper;
import svenhjol.strange.structure.DataBlockProcessor;

import java.util.Arrays;

public class StrangeStructures {
    public static void init() {
        StructureHelper.SINGLE_POOL_ELEMENT_PROCESSORS.addAll(Arrays.asList(
            new BlockIgnoreStructureProcessor(ImmutableList.of(Blocks.GRAY_STAINED_GLASS)),
            new DataBlockProcessor()
        ));
    }
}
