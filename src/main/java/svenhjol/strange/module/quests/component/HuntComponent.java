package svenhjol.strange.module.quests.component;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import svenhjol.strange.module.quests.IQuestComponent;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.definition.QuestDefinition;
import svenhjol.strange.module.quests.helper.QuestDefinitionHelper;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class HuntComponent implements IQuestComponent {
    public static final String ID = "hunt";

    public static final String ENTITIES_TAG = "entities";
    public static final String ENTITY_COUNT_TAG = "entity_count";
    public static final String ENTITY_KILLED_TAG = "entity_killed";

    private final Quest quest;
    private final Map<ResourceLocation, Integer> entities = new HashMap<>();
    private final Map<ResourceLocation, Integer> killed = new HashMap<>();
    private final Map<ResourceLocation, Integer> satisfied = new HashMap<>(); // not stored in nbt

    public HuntComponent(Quest quest) {
        this.quest = quest;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isPresent() {
        return entities.size() > 0;
    }

    @Override
    public CompoundTag save() {
        var out = new CompoundTag();
        var entitiesTag = new CompoundTag();
        var countTag = new CompoundTag();
        var killedTag = new CompoundTag();

        if (!entities.isEmpty()) {
            var index = 0;
            for (ResourceLocation id : entities.keySet()) {
                var entityIndex = Integer.toString(index);
                var entityCount = entities.get(id);
                var entityKilled = killed.getOrDefault(id, 0);

                // Write to tags at stringified index.
                entitiesTag.putString(entityIndex, id.toString());
                countTag.putInt(entityIndex, entityCount);
                killedTag.putInt(entityIndex, entityKilled);

                index++;
            }

            out.put(ENTITIES_TAG, entitiesTag);
            out.put(ENTITY_COUNT_TAG, countTag);
            out.put(ENTITY_KILLED_TAG, killedTag);
        }

        return out;
    }

    @Override
    public void load(CompoundTag nbt) {
        CompoundTag entitiesTag = (CompoundTag) nbt.get(ENTITIES_TAG);
        CompoundTag countTag = (CompoundTag) nbt.get(ENTITY_COUNT_TAG);
        CompoundTag killedTag = (CompoundTag) nbt.get(ENTITY_KILLED_TAG);

        entities.clear();
        killed.clear();

        if (entitiesTag != null && entitiesTag.size() > 0 && countTag != null) {
            for (int i = 0; i < entitiesTag.size(); i++) {
                // read the data from the tags at the specified index
                String tagIndex = String.valueOf(i);
                ResourceLocation entityId = ResourceLocation.tryParse(entitiesTag.getString(tagIndex));
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
        if (!isPresent()) return true;
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
        if (attacker instanceof OwnableEntity owned) {
            attacker = owned.getOwner();
        }
        if (attacker instanceof ServerPlayer player) {
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
