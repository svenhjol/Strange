package svenhjol.strange.scroll.populator;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import svenhjol.strange.scroll.JsonDefinition;
import svenhjol.strange.scroll.tag.QuestTag;

import java.util.HashMap;
import java.util.Map;

public class HuntPopulator extends Populator {
    public HuntPopulator(PlayerEntity player, QuestTag quest, JsonDefinition definition) {
        super(player, quest, definition);
    }

    @Override
    public void populate() {
        Map<String, String> hunt = definition.getHunt();
        Map<Identifier, Integer> entities = new HashMap<>();

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
