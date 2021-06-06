package svenhjol.strange.module.ruins.builds;

import com.google.common.collect.ImmutableList;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import svenhjol.charm.mixin.accessor.ProcessorListsAccessor;
import svenhjol.charm.world.CharmStructure;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class StoneFort extends CharmStructure {
    private final Map<String, Integer> CORRIDORS = new HashMap<>();
    private final Map<String, Integer> ROOMS = new HashMap<>();
    private final Map<String, Integer> ENDS = new HashMap<>();

    private static StructureProcessorList GRASS_PROCESSOR;

    public StoneFort() {
        super(Strange.MOD_ID, "ruins", "stone_fort");

        GRASS_PROCESSOR = ProcessorListsAccessor.invokeRegister("strange:stone_fort_grass_processor", ImmutableList.of(new RuleProcessor(ImmutableList.of(
            new ProcessorRule(new BlockMatchTest(Blocks.GRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.COARSE_DIRT.defaultBlockState()),
            new ProcessorRule(new RandomBlockMatchTest(Blocks.GRASS_BLOCK, 0.1F), AlwaysTrueTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.defaultBlockState()),
            new ProcessorRule(new BlockMatchTest(Blocks.GRASS_BLOCK), new BlockMatchTest(Blocks.WATER), Blocks.DIRT.defaultBlockState()),
            new ProcessorRule(new BlockMatchTest(Blocks.GRASS_BLOCK), new BlockMatchTest(Blocks.AIR), Blocks.COBBLESTONE.defaultBlockState()),
            new ProcessorRule(new BlockMatchTest(Blocks.GRASS_BLOCK), new BlockMatchTest(Blocks.AIR), Blocks.MOSSY_COBBLESTONE.defaultBlockState()))
        )));

        addStart("start1", 1, ProcessorLists.STREET_PLAINS);
        addStart("start2", 1, ProcessorLists.STREET_PLAINS);

        CORRIDORS.put("corridor1", 1);
        CORRIDORS.put("corridor2", 1);
        CORRIDORS.put("corridor3", 1);
        CORRIDORS.put("corridor4", 1);

        ROOMS.put("tower1", 1);
        ROOMS.put("tower2", 1);
        ROOMS.put("tower3", 1);
        ROOMS.put("tower4", 1);

        ENDS.put("end1", 1);

        registerPool("corridors", CORRIDORS, StructureTemplatePool.Projection.TERRAIN_MATCHING, GRASS_PROCESSOR);
        registerPool("rooms", ROOMS, StructureTemplatePool.Projection.TERRAIN_MATCHING, GRASS_PROCESSOR);
        registerPool("ends", ENDS, StructureTemplatePool.Projection.TERRAIN_MATCHING, GRASS_PROCESSOR);
    }
}
