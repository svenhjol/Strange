package svenhjol.strange.ruins.builds;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Blocks;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.processor.RuleStructureProcessor;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorLists;
import net.minecraft.structure.processor.StructureProcessorRule;
import net.minecraft.structure.rule.AlwaysTrueRuleTest;
import net.minecraft.structure.rule.BlockMatchRuleTest;
import net.minecraft.structure.rule.RandomBlockMatchRuleTest;
import svenhjol.charm.base.structure.BaseStructure;
import svenhjol.charm.mixin.accessor.StructureProcessorListsAccessor;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class StoneFort extends BaseStructure {
    private final Map<String, Integer> CORRIDORS = new HashMap<>();
    private final Map<String, Integer> ROOMS = new HashMap<>();
    private final Map<String, Integer> ENDS = new HashMap<>();

    private static StructureProcessorList GRASS_PROCESSOR;

    public StoneFort() {
        super(Strange.MOD_ID, "ruins", "stone_fort");

        GRASS_PROCESSOR = StructureProcessorListsAccessor.invokeRegister("strange:stone_fort_grass_processor", ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(
            new StructureProcessorRule(new BlockMatchRuleTest(Blocks.GRASS_BLOCK), new BlockMatchRuleTest(Blocks.WATER), Blocks.COARSE_DIRT.getDefaultState()),
            new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.GRASS_BLOCK, 0.1F), AlwaysTrueRuleTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.getDefaultState()),
            new StructureProcessorRule(new BlockMatchRuleTest(Blocks.GRASS_BLOCK), new BlockMatchRuleTest(Blocks.WATER), Blocks.DIRT.getDefaultState()),
            new StructureProcessorRule(new BlockMatchRuleTest(Blocks.GRASS_BLOCK), new BlockMatchRuleTest(Blocks.AIR), Blocks.COBBLESTONE.getDefaultState()),
            new StructureProcessorRule(new BlockMatchRuleTest(Blocks.GRASS_BLOCK), new BlockMatchRuleTest(Blocks.AIR), Blocks.MOSSY_COBBLESTONE.getDefaultState()))
        )));

        addStart("start1", 1, StructureProcessorLists.STREET_PLAINS);

        CORRIDORS.put("corridor1", 1);
        CORRIDORS.put("corridor2", 1);
        CORRIDORS.put("corridor3", 1);
        CORRIDORS.put("corridor4", 1);

        ROOMS.put("tower1", 1);
        ROOMS.put("tower2", 1);
        ROOMS.put("tower3", 1);
        ROOMS.put("tower4", 1);

        ENDS.put("end1", 1);

        registerPool("corridors", CORRIDORS, StructurePool.Projection.TERRAIN_MATCHING, GRASS_PROCESSOR);
        registerPool("rooms", ROOMS, StructurePool.Projection.TERRAIN_MATCHING, GRASS_PROCESSOR);
        registerPool("ends", ENDS, StructurePool.Projection.TERRAIN_MATCHING, GRASS_PROCESSOR);
    }
}
