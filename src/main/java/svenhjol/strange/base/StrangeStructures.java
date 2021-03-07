package svenhjol.strange.base;

import net.minecraft.block.Blocks;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.structure.DataBlockProcessor;
import svenhjol.strange.rubble.Rubble;
import svenhjol.strange.runestones.Runestones;
import svenhjol.strange.runestones.RunestonesHelper;

public class StrangeStructures {
    public static void init() {
        DataBlockProcessor.callbacks.put("rubble", processor -> {
            if (ModuleHandler.enabled("strange:rubble") && processor.withChance(0.9F)) {
                processor.state = Rubble.RUBBLE.getDefaultState();
            } else {
                processor.state = Blocks.GRAVEL.getDefaultState();
            }
        });

        DataBlockProcessor.callbacks.put("runestone", processor -> {
            if (ModuleHandler.enabled("strange:runestones") && processor.withChance(0.7F)) {
                processor.state = Runestones.RUNESTONE_BLOCKS.get(processor.random.nextInt(RunestonesHelper.NUMBER_OF_RUNES)).getDefaultState();
            } else {
                processor.state = Blocks.STONE.getDefaultState();
            }
        });
    }
}
