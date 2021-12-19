package svenhjol.strange.module.quests.component;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import svenhjol.strange.module.quests.IQuestComponent;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.definition.QuestDefinition;
import svenhjol.strange.module.quests.helper.QuestDefinitionHelper;

import java.util.HashMap;
import java.util.Map;

public class HuntComponent implements IQuestComponent {
    public static final String TAG_ENTITY_DATA = "entity_data";
    public static final String TAG_ENTITY_COUNT = "entity_count";
    public static final String TAG_ENTITY_KILLED = "entity_killed";

    private final Quest quest;
    private final Map<ResourceLocation, Integer> entities = new HashMap<>();
    private final Map<ResourceLocation, Integer> killed = new HashMap<>();
    private final Map<ResourceLocation, Integer> satisfied = new HashMap<>(); // not stored in nbt

    public HuntComponent(Quest quest) {
        this.quest = quest;
    }

    @Override
    public String getId() {
        return "hunt";
    }

    @Override
    public boolean isEmpty() {
        return entities.isEmpty();
    }

    @Override
    public CompoundTag save() {
        CompoundTag outTag = new CompoundTag();
        CompoundTag dataTag = new CompoundTag();
        CompoundTag countTag = new CompoundTag();
        CompoundTag killedTag = new CompoundTag();

        if (entities.isEmpty()) return null;

        int index = 0;
        for (ResourceLocation entityId : entities.keySet()) {
            String entityIndex = Integer.toString(index);
            int entityCount = entities.get(entityId);
            int entityKilled = killed.getOrDefault(entityId, 0);

            // write the data to the tags at the specified index
            dataTag.putString(entityIndex, entityId.toString());
            countTag.putInt(entityIndex, entityCount);
            killedTag.putInt(entityIndex, entityKilled);

            index++;
        }

        outTag.put(TAG_ENTITY_DATA, dataTag);
        outTag.put(TAG_ENTITY_COUNT, countTag);
        outTag.put(TAG_ENTITY_KILLED, killedTag);

        return outTag;
    }

    @Override
    public void load(CompoundTag nbt) {
        CompoundTag dataTag = (CompoundTag) nbt.get(TAG_ENTITY_DATA);
        CompoundTag countTag = (CompoundTag) nbt.get(TAG_ENTITY_COUNT);
        CompoundTag killedTag = (CompoundTag) nbt.get(TAG_ENTITY_KILLED);

        entities.clear();
        killed.clear();

        if (dataTag != null && dataTag.size() > 0 && countTag != null) {
            for (int i = 0; i < dataTag.size(); i++) {
                // read the data from the tags at the specified index
                String tagIndex = String.valueOf(i);
                ResourceLocation entityId = ResourceLocation.tryParse(dataTag.getString(tagIndex));
                if (entityId == null) continue;

                int entityCount = countTag.getInt(tagIndex);
                int entityKilled = killedTag != null ? killedTag.getInt(tagIndex) : 0;

                entities.put(entityId, entityCount);
                killed.put(entityId, entityKilled);
            }
        }
    }

    @Override
    public boolean start(Player player) {
        if (player.level.isClientSide) return false;

        QuestDefinition definition = quest.getDefinition();
        Map<String, String> huntDefinition = definition.getHunt();
        if (huntDefinition == null || huntDefinition.isEmpty()) return true;

        HuntComponent hunt = quest.getComponent(HuntComponent.class);
        Map<ResourceLocation, Integer> entities = new HashMap<>();

        for (String id : huntDefinition.keySet()) {
            ResourceLocation entityId = QuestDefinitionHelper.getEntityIdFromKey(id);
            if (entityId == null) continue;
            int count = QuestDefinitionHelper.getCountFromValue(huntDefinition.get(id), 1);
            entities.put(entityId, count);
        }

        if (entities.isEmpty()) return false;
        entities.forEach(hunt::addEntity);
        return true;
    }

    @Override
    public void update(Player player) {
        satisfied.clear();

        entities.forEach((id, count) -> {
            int countKilled = killed.getOrDefault(id, 0);
            satisfied.put(id, countKilled >= count ? count : countKilled);
        });
    }

    @Override
    public boolean isSatisfied(Player player) {
        if (entities.isEmpty()) return true;
        var count = 0;

        for (ResourceLocation res : entities.keySet()) {
            if (satisfied.containsKey(res) && satisfied.get(res) == (int)entities.get(res)) {
                count++;
            }
        }

        return entities.size() == count;
    }

    @Override
    public void entityKilled(LivingEntity entity, Entity attacker) {
        if (!(attacker instanceof ServerPlayer player)) return;

        if (quest.getOwner().equals(player.getUUID())) {
            ResourceLocation id = Registry.ENTITY_TYPE.getKey(entity.getType());

            if (entities.containsKey(id)) {
                int max = entities.get(id);
                int count = killed.getOrDefault(id, 0);
                killed.put(id, Math.min(max, count + 1));

                quest.setDirty();
                quest.update(player);
            }
        }
    }

    public void addEntity(ResourceLocation entity, int count) {
        entities.put(entity, count);
    }

    public Map<ResourceLocation, Integer> getEntities() {
        return entities;
    }

    public Map<ResourceLocation, Integer> getKilled() {
        return killed;
    }

    public Map<ResourceLocation, Integer> getSatisfied() {
        return satisfied;
    }
}
