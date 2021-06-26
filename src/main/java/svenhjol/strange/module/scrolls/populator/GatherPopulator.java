package svenhjol.strange.module.scrolls.populator;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.module.scrolls.nbt.Quest;

import java.util.List;
import java.util.Map;

public class GatherPopulator extends BasePopulator {
    public GatherPopulator(ServerPlayer player, Quest quest) {
        super(player, quest);
    }

    @Override
    public void populate() {
        Map<String, Map<String, Map<String, String>>> gather = definition.getGather();
        if (!gather.containsKey(ITEMS_NBT))
            return;

        List<ItemStack> items = parseItems(gather.get(ITEMS_NBT), 4, false);
        items.forEach(quest.getGather()::addItem);
    }
}
