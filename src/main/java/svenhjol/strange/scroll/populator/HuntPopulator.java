package svenhjol.strange.scroll.populator;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import svenhjol.strange.scroll.tag.Quest;

import java.util.HashMap;
import java.util.Map;

public class HuntPopulator extends BasePopulator {
    public HuntPopulator(ServerPlayerEntity player, Quest quest) {
        super(player, quest);
    }

    @Override
    public void populate() {
        Map<String, String> hunt = definition.getHunt();
        Map<Identifier, Integer> entities = new HashMap<>();

        if (hunt.isEmpty())
            return;

        for (String id : hunt.keySet()) {
            Identifier entityId = getEntityIdFromKey(id);
            if (entityId == null)
                continue;

            int count = getCountFromValue(hunt.get(id), 1, false);
            entities.put(entityId, count);
        }

        entities.forEach(quest.getHunt()::addEntity);
    }
}
