package svenhjol.strange.scrolls.populator;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import svenhjol.strange.scrolls.JsonDefinition;
import svenhjol.strange.scrolls.tag.Quest;

import java.util.List;
import java.util.Map;

public class GatherPopulator extends Populator {
    public static final String ITEMS = "items";

    public GatherPopulator(ServerPlayerEntity player, Quest quest, JsonDefinition definition) {
        super(player, quest, definition);
    }

    @Override
    public void populate() {
        Map<String, Map<String, Map<String, String>>> gather = definition.getGather();
        if (!gather.containsKey(ITEMS))
            return;

        List<ItemStack> items = parseItems(gather.get(ITEMS), 4, false);
        items.forEach(quest.getGather()::addItem);
    }
}
