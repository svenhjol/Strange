package svenhjol.strange.base;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Blocks;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import svenhjol.meson.MesonMod;
import svenhjol.strange.helper.StructureHelper;

public class StrangeStructures {
    public static void init(MesonMod mod) {
        StructureHelper.SINGLE_POOL_ELEMENT_PROCESSORS.add(
            new BlockIgnoreStructureProcessor(ImmutableList.of(Blocks.GRAY_STAINED_GLASS))
        );
    }
}
