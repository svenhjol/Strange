package svenhjol.strange.scroll.tag;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.*;

public class BossTag implements ITag {
    public static final String BOSS_ENTITIES = "boss_entities";
    public static final String BOSS_COUNT = "boss_count";
    public static final String BOSS_HEALTH = "boss_health";
    public static final String BOSS_EFFECTS = "boss_effects";

    public static final String SUPPORT_ENTITIES = "support_entities";
    public static final String SUPPORT_COUNT = "support_count";
    public static final String SUPPORT_HEALTH = "support_health";
    public static final String SUPPORT_EFFECTS = "support_effects";

    public static final String KILLED = "killed";
    public static final String COUNT = "count";
    public static final String SPAWNED = "spawned";
    public static final String STRUCTURE = "structure";
    public static final String DIMENSION = "dimension";

    private QuestTag questTag;
    private int killed;
    private int count;
    private BlockPos structure;
    private Identifier dimension;
    private boolean spawned;

    private Map<Identifier, Integer> bossEntities = new HashMap<>();
    private Map<Identifier, Integer> bossHealth = new HashMap<>();
    private Map<Identifier, List<String>> bossEffects = new HashMap<>();
    private Map<Identifier, Integer> supportEntities = new HashMap<>();
    private Map<Identifier, Integer> supportHealth = new HashMap<>();
    private Map<Identifier, List<String>> supportEffects = new HashMap<>();

    // these are dynamically generated, not stored in nbt
    private Map<Identifier, String> names = new HashMap<>();

    public BossTag(QuestTag questTag) {
        this.questTag = questTag;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag outTag = new CompoundTag();
        CompoundTag bossEntitiesTag = new CompoundTag();
        CompoundTag bossCountTag = new CompoundTag();
        CompoundTag bossHealthTag = new CompoundTag();
        CompoundTag bossEffectsTag = new CompoundTag();
        CompoundTag supportEntitiesTag = new CompoundTag();
        CompoundTag supportCountTag = new CompoundTag();
        CompoundTag supportHealthTag = new CompoundTag();
        CompoundTag supportEffectsTag = new CompoundTag();

        // write boss entity data
        if (!bossEntities.isEmpty()) {
            int index = 0;

            for (Identifier entityId : bossEntities.keySet()) {
                String entityIndex = Integer.toString(index);
                int entityCount = bossEntities.get(entityId);
                int entityHealth = bossHealth.get(entityId);

                bossEntitiesTag.putString(entityIndex, entityId.toString());
                bossCountTag.putInt(entityIndex, entityCount);
                bossHealthTag.putInt(entityIndex, entityHealth);

                if (bossEffects.size() > 0) {
                    CompoundTag tag = new CompoundTag();
                    List<String> effects = bossEffects.get(entityId);
                    for (int i = 0; i < effects.size(); i++) {
                        tag.putString(String.valueOf(i), effects.get(i));
                    }
                    bossEffectsTag.put(entityIndex, tag);
                }
            }

            outTag.put(BOSS_ENTITIES, bossEffectsTag);
            outTag.put(BOSS_COUNT, bossCountTag);
            outTag.put(BOSS_HEALTH, bossHealthTag);
            outTag.put(BOSS_EFFECTS, bossEffectsTag);
        }

        // write support entity data
        if (!supportEntities.isEmpty()) {
            int index = 0;

            for (Identifier entityId : supportEntities.keySet()) {
                String entityIndex = Integer.toString(index);
                int entityCount = supportEntities.get(entityId);
                int entityHealth = supportHealth.get(entityId);

                supportEntitiesTag.putString(entityIndex, entityId.toString());
                supportCountTag.putInt(entityIndex, entityCount);
                supportHealthTag.putInt(entityIndex, entityHealth);

                if (supportEffects.size() > 0) {
                    CompoundTag tag = new CompoundTag();
                    List<String> effects = supportEffects.get(entityId);
                    for (int i = 0; i < effects.size(); i++) {
                        tag.putString(String.valueOf(i), effects.get(i));
                    }
                    supportEffectsTag.put(entityIndex, tag);
                }
            }

            outTag.put(SUPPORT_ENTITIES, supportEffectsTag);
            outTag.put(SUPPORT_COUNT, supportCountTag);
            outTag.put(SUPPORT_HEALTH, supportHealthTag);
            outTag.put(SUPPORT_EFFECTS, supportEffectsTag);
        }

        outTag.putInt(KILLED, killed);
        outTag.putInt(COUNT, count);
        outTag.putBoolean(SPAWNED, spawned);

        if (dimension != null)
            outTag.putString(DIMENSION, dimension.toString());

        if (structure != null)
            outTag.putLong(STRUCTURE, structure.asLong());

        return outTag;
    }

    @Override
    public void fromTag(CompoundTag fromTag) {
        structure = fromTag.contains(STRUCTURE) ? BlockPos.fromLong(fromTag.getLong(STRUCTURE)) : null;
        dimension = Identifier.tryParse(fromTag.getString(DIMENSION));
        killed = fromTag.contains(KILLED) ? fromTag.getInt(KILLED) : 0;
        count = fromTag.contains(COUNT) ? fromTag.getInt(COUNT) : 0;
        spawned = fromTag.contains(SPAWNED) && fromTag.getBoolean(SPAWNED);

        if (dimension == null)
            dimension = new Identifier("minecraft", "overworld");

        bossEntities = new HashMap<>();
        bossHealth = new HashMap<>();
        bossEffects = new HashMap<>();
        supportEntities = new HashMap<>();
        supportHealth = new HashMap<>();
        supportEffects = new HashMap<>();

        CompoundTag bossEntitiesTag = (CompoundTag)fromTag.get(BOSS_ENTITIES);
        CompoundTag bossCountTag = (CompoundTag)fromTag.get(BOSS_COUNT);
        CompoundTag bossHealthTag = (CompoundTag)fromTag.get(BOSS_HEALTH);
        CompoundTag bossEffectsTag = (CompoundTag)fromTag.get(BOSS_EFFECTS);
        CompoundTag supportEntitiesTag = (CompoundTag)fromTag.get(SUPPORT_ENTITIES);
        CompoundTag supportCountTag = (CompoundTag)fromTag.get(SUPPORT_COUNT);
        CompoundTag supportHealthTag = (CompoundTag)fromTag.get(SUPPORT_HEALTH);
        CompoundTag supportEffectsTag = (CompoundTag)fromTag.get(SUPPORT_EFFECTS);

        if (bossEntitiesTag != null && bossEntitiesTag.getSize() > 0 && bossCountTag != null && bossHealthTag != null) {
            for (int i = 0; i < bossEntitiesTag.getSize(); i++) {
                // read data from the tags at specified index
                String tagIndex = String.valueOf(i);
                Identifier entityId = Identifier.tryParse(bossEntitiesTag.getString(tagIndex));
                if (entityId == null)
                    continue;

                int count = bossCountTag.getInt(tagIndex);
                bossEntities.put(entityId, count);

                int entityHealth = bossHealthTag.getInt(tagIndex);
                bossHealth.put(entityId, entityHealth);

                // parse entity effects back into arraylist
                if (bossEffectsTag != null && bossEffectsTag.getSize() > 0) {
                    CompoundTag tag = (CompoundTag) bossEffectsTag.get(tagIndex);
                    bossEffects.put(entityId, new ArrayList<>());
                    if (tag != null && tag.getSize() > 0) {
                        for (int j = 0; j < tag.getSize(); j++) {
                            bossEffects.get(entityId).add(tag.getString(String.valueOf(j)));
                        }
                    }
                }
            }
        }

        if (supportEntitiesTag != null && supportEntitiesTag.getSize() > 0 && supportCountTag != null && supportHealthTag != null) {
            for (int i = 0; i < supportEntitiesTag.getSize(); i++) {
                // read data from the tags at specified index
                String tagIndex = String.valueOf(i);
                Identifier entityId = Identifier.tryParse(supportEntitiesTag.getString(tagIndex));
                if (entityId == null)
                    continue;

                int count = supportCountTag.getInt(tagIndex);
                supportEntities.put(entityId, count);

                int entityHealth = supportHealthTag.getInt(tagIndex);
                supportHealth.put(entityId, entityHealth);

                // parse entity effects back into arraylist
                if (supportEffectsTag != null && supportEffectsTag.getSize() > 0) {
                    CompoundTag tag = (CompoundTag) supportEffectsTag.get(tagIndex);
                    supportEffects.put(entityId, new ArrayList<>());
                    if (tag != null && tag.getSize() > 0) {
                        for (int j = 0; j < tag.getSize(); j++) {
                            supportEffects.get(entityId).add(tag.getString(String.valueOf(j)));
                        }
                    }
                }
            }
        }
    }

    public void addBossEntity(Identifier entity, int count, int health, List<String> effects) {
        bossEntities.put(entity, count);
        bossHealth.put(entity, health);
        bossEffects.put(entity, effects);

        this.count++;
    }

    public void addSupportEntity(Identifier entity, int count, int health, List<String> effects) {
        supportEntities.put(entity, count);
        supportHealth.put(entity, health);
        supportEffects.put(entity, effects);
    }

    public boolean isSatisfied() {
        if (count == 0)
            return true;

        return killed >= count;
    }

    public void update(PlayerEntity player) {
        bossEntities.forEach((id, count) -> {
            Optional<EntityType<?>> optionalEntityType = Registry.ENTITY_TYPE.getOrEmpty(id);
            optionalEntityType.ifPresent(entityType -> names.put(id, entityType.getName().getString()));
        });
    }

    public void playerKilledEntity(PlayerEntity player, LivingEntity entity) {
        entity.addScoreboardTag(
    }
}
