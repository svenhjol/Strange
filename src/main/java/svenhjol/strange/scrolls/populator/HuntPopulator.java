package svenhjol.strange.scrolls.populator;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import svenhjol.strange.scrolls.JsonDefinition;
import svenhjol.strange.scrolls.tag.Quest;

import java.util.HashMap;
import java.util.Map;

public class HuntPopulator extends Populator {
    public HuntPopulator(ServerPlayerEntity player, Quest quest, JsonDefinition definition) {
        super(player, quest, definition);
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

            int count = getCountFromValue(hunt.get(id), false);
            entities.put(entityId, count);
        }

        entities.forEach(quest.getHunt()::addEntity);
    }
}
