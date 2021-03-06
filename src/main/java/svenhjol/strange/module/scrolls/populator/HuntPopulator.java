package svenhjol.strange.module.scrolls.populator;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.strange.module.scrolls.nbt.Quest;

import java.util.HashMap;
import java.util.Map;

public class HuntPopulator extends BasePopulator {
    public HuntPopulator(ServerPlayer player, Quest quest) {
        super(player, quest);
    }

    @Override
    public void populate() {
        Map<String, String> hunt = definition.getHunt();
        Map<ResourceLocation, Integer> entities = new HashMap<>();

        if (hunt.isEmpty())
            return;

        for (String id : hunt.keySet()) {
            ResourceLocation entityId = getEntityIdFromKey(id);
            if (entityId == null)
                continue;

            int count = getCountFromValue(hunt.get(id), 1, false);
            entities.put(entityId, count);
        }

        entities.forEach(quest.getHunt()::addEntity);
    }
}
