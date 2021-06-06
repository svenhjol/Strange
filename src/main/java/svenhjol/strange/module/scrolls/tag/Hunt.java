package svenhjol.strange.module.scrolls.tag;

import svenhjol.strange.module.scrolls.ScrollsHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class Hunt implements ISerializable {
    public static final String ENTITY_DATA = "entity_data";
    public static final String ENTITY_COUNT = "entity_count";
    public static final String ENTITY_KILLED = "entity_killed";

    private Quest quest;
    private Map<ResourceLocation, Integer> entities = new HashMap<>();
    private Map<ResourceLocation, Integer> killed = new HashMap<>();

    // these are dynamically generated, not stored in nbt
    private Map<ResourceLocation, Boolean> satisfied = new HashMap<>();
    private Map<ResourceLocation, String> names = new HashMap<>();

    public Hunt(Quest quest) {
        this.quest = quest;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag outTag = new CompoundTag();
        CompoundTag dataTag = new CompoundTag();
        CompoundTag countTag = new CompoundTag();
        CompoundTag killedTag = new CompoundTag();

        if (!entities.isEmpty()) {
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
        }

        outTag.put(ENTITY_DATA, dataTag);
        outTag.put(ENTITY_COUNT, countTag);
        outTag.put(ENTITY_KILLED, killedTag);

        return outTag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        CompoundTag dataTag = (CompoundTag)tag.get(ENTITY_DATA);
        CompoundTag countTag = (CompoundTag)tag.get(ENTITY_COUNT);
        CompoundTag killedTag = (CompoundTag)tag.get(ENTITY_KILLED);

        entities = new HashMap<>();
        killed = new HashMap<>();

        if (dataTag != null && dataTag.size() > 0 && countTag != null) {
            for (int i = 0; i < dataTag.size(); i++) {
                // read the data from the tags at the specified index
                String tagIndex = String.valueOf(i);
                ResourceLocation entityId = ResourceLocation.tryParse(dataTag.getString(tagIndex));
                if (entityId == null)
                    continue;

                int entityCount = countTag.getInt(tagIndex);
                int entityKilled = killedTag != null ? killedTag.getInt(tagIndex) : 0;

                entities.put(entityId, entityCount);
                killed.put(entityId, entityKilled);
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

    public Map<ResourceLocation, Boolean> getSatisfied() {
        return satisfied;
    }

    public Map<ResourceLocation, String> getNames() {
        return names;
    }

    public boolean isSatisfied() {
        if (entities.isEmpty())
            return true;

        return satisfied.size() == entities.size() && getSatisfied().values().stream().allMatch(r -> r);
    }

    public void update(Player player) {
        satisfied.clear();

        entities.forEach((id, count) -> {
            int countKilled = killed.getOrDefault(id, 0);
            satisfied.put(id, countKilled >= count);

            Optional<EntityType<?>> optionalEntityType = Registry.ENTITY_TYPE.getOptional(id);
            optionalEntityType.ifPresent(entityType -> names.put(id, entityType.getDescription().getString()));
        });
    }

    public void entityKilled(LivingEntity entity, Entity attacker) {
        if (!(attacker instanceof ServerPlayer))
            return;

        ServerPlayer player = (ServerPlayer) attacker;

        // must be the player who owns the quest
        if (quest.getOwner().equals(player.getUUID()) || quest.getOwner().equals(ScrollsHelper.ANY_UUID)) {
            ResourceLocation id = Registry.ENTITY_TYPE.getKey(entity.getType());

            if (entities.containsKey(id)) {
                Integer count = killed.getOrDefault(id, 0);
                killed.put(id, count + 1);

                quest.setDirty(true);
                quest.update(player);
            }
        }
    }
}
