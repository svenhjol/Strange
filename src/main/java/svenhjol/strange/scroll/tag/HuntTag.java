package svenhjol.strange.scroll.tag;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class HuntTag implements ITag {
    public static final String ENTITY_DATA = "entity_data";
    public static final String ENTITY_COUNT = "entity_count";
    public static final String ENTITY_KILLED = "entity_killed";

    private QuestTag questTag;
    private Map<Identifier, Integer> entities = new HashMap<>();
    private Map<Identifier, Integer> killed = new HashMap<>();
    private Map<Identifier, Boolean> satisfied = new HashMap<>(); // this is dynamically generated, not stored in nbt
    private Map<Identifier, String> names = new HashMap<>(); // this is dynamically generated, not stored in nbt

    public HuntTag(QuestTag questTag) {
        this.questTag = questTag;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag outTag = new CompoundTag();
        CompoundTag dataTag = new CompoundTag();
        CompoundTag countTag = new CompoundTag();
        CompoundTag killedTag = new CompoundTag();

        if (!entities.isEmpty()) {
            int index = 0;
            for (Identifier entityId : entities.keySet()) {
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

        if (dataTag != null && dataTag.getSize() > 0 && countTag != null) {
            for (int i = 0; i < dataTag.getSize(); i++) {
                // read the data from the tags at the specified index
                String tagIndex = String.valueOf(i);
                Identifier entityId = Identifier.tryParse(dataTag.getString(tagIndex));
                if (entityId == null)
                    continue;

                int entityCount = countTag.getInt(tagIndex);
                int entityKilled = killedTag != null ? killedTag.getInt(tagIndex) : 0;

                entities.put(entityId, entityCount);
                killed.put(entityId, entityKilled);
            }
        }
    }

    public void addEntity(Identifier entity, int count) {
        entities.put(entity, count);
    }

    public Map<Identifier, Integer> getEntities() {
        return entities;
    }

    public Map<Identifier, Integer> getKilled() {
        return killed;
    }

    public Map<Identifier, Boolean> getSatisfied() {
        return satisfied;
    }

    public Map<Identifier, String> getNames() {
        return names;
    }

    public void update(PlayerEntity player) {
        satisfied.clear();

        entities.forEach((id, count) -> {
            int countKilled = killed.getOrDefault(id, 0);
            satisfied.put(id, countKilled >= count);

            if (Registry.ENTITY_TYPE.containsId(id))
                names.put(id, Registry.ENTITY_TYPE.get(id).getName().getString());
        });
    }
}
