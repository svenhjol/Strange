package svenhjol.strange.scroll.populator;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import svenhjol.strange.scroll.JsonDefinition;
import svenhjol.strange.scroll.tag.Quest;

import java.util.HashMap;
import java.util.Map;

public class GatherPopulator extends Populator {
    public GatherPopulator(ServerPlayerEntity player, Quest quest, JsonDefinition definition) {
        super(player, quest, definition);
    }

    @Override
    public void populate() {
        Map<String, String> gather = definition.getGather();
        Map<ItemStack, Integer> items = new HashMap<>();

        if (gather.isEmpty())
            return;

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
