package svenhjol.strange.module.scrolls.populator;

import svenhjol.strange.module.scrolls.tag.Quest;

import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class GatherPopulator extends BasePopulator {
    public static final String ITEMS = "items";

    public GatherPopulator(ServerPlayer player, Quest quest) {
        super(player, quest);
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
