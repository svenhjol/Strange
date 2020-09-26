package svenhjol.strange.scroll.populator;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.strange.scroll.BasePopulator;
import svenhjol.strange.scroll.JsonDefinition;
import svenhjol.strange.scroll.tag.QuestTag;

import java.util.HashMap;
import java.util.Map;

public class GatherGenerator extends BasePopulator {
    public GatherGenerator(World world, BlockPos pos, QuestTag quest, JsonDefinition definition) {
        super(world, pos, quest, definition);
    }

    @Override
    public void populate() {
        Map<String, String> gather = definition.getGather();
        Map<ItemStack, Integer> items = new HashMap<>();

        for (String stackName : gather.keySet()) {
            ItemStack stack = getItemFromKey(stackName);
            if (stack == null)
                continue;

            int count = getCountFromValue(gather.get(stackName), false);
            items.put(stack, count);
        }

        items.forEach(quest.getGather()::addItem);
    }
}
