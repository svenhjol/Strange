package svenhjol.strange.scroll.populator;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.strange.scroll.JsonDefinition;
import svenhjol.strange.scroll.tag.QuestTag;

import java.util.HashMap;
import java.util.Map;

public class HuntPopulator extends Populator {
    public HuntPopulator(World world, BlockPos pos, QuestTag quest, JsonDefinition definition) {
        super(world, pos, quest, definition);
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
