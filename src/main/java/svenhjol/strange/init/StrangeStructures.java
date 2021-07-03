package svenhjol.strange.init;

import net.minecraft.world.level.block.Blocks;
import svenhjol.charm.handler.ModuleHandler;
import svenhjol.charm.structure.CharmDataBlockProcessor;
import svenhjol.strange.module.runestones.RunestonesHelper;
import svenhjol.strange.module.rubble.Rubble;
import svenhjol.strange.module.runestones.Runestones;

public class StrangeStructures {
    public static void init() {
        CharmDataBlockProcessor.callbacks.put("rubble", processor -> {
            if (ModuleHandler.enabled("strange:rubble") && processor.withChance(0.9F)) {
                processor.state = Rubble.RUBBLE.defaultBlockState();
            } else {
                processor.state = Blocks.GRAVEL.defaultBlockState();
            }
        });

        CharmDataBlockProcessor.callbacks.put("runestone", processor -> {
            if (ModuleHandler.enabled("strange:runestones") && processor.withChance(0.7F)) {
                processor.state = Runestones.RUNESTONE_BLOCKS.get(processor.random.nextInt(RunestonesHelper.NUMBER_OF_RUNES)).defaultBlockState();
            } else {
                processor.state = Blocks.AIR.defaultBlockState();
            }
        });
    }
}
