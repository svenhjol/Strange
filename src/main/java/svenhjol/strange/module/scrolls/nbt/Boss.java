package svenhjol.strange.module.scrolls.nbt;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import svenhjol.charm.helper.PosHelper;
import svenhjol.strange.module.scrolls.ScrollHelper;
import svenhjol.strange.module.scrolls.populator.BossPopulator;

import java.util.*;

public class Boss implements IQuestSerializable {
    public static final String TARGET_ENTITIES_NBT = "target_entities";
    public static final String TARGET_COUNT_NBT = "target_count";
    public static final String TARGET_KILLED_NBT = "target_killed";
    public static final String SPAWNED_NBT = "spawned";
    public static final String STRUCTURE_NBT = "structure";
    public static final String DIMENSION_NBT = "dimension";

    private final Quest quest;
    private BlockPos structure;
    private ResourceLocation dimension;
    private boolean spawned;

    private Map<ResourceLocation, Integer> entities = new HashMap<>();
    private Map<ResourceLocation, Integer> killed = new HashMap<>();

    // these are dynamically generated, not stored in nbt
    private final Map<ResourceLocation, Boolean> satisfied = new HashMap<>();
    private final Map<ResourceLocation, String> names = new HashMap<>();

    public Boss(Quest quest) {
        this.quest = quest;
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag outTag = new CompoundTag();
        CompoundTag entitiesTag = new CompoundTag();
        CompoundTag countTag = new CompoundTag();
        CompoundTag killedTag = new CompoundTag();

        // write boss entity data
        if (!entities.isEmpty()) {
            int index = 0;

            for (ResourceLocation id : entities.keySet()) {
                String tagIndex = Integer.toString(index);
                int entityCount = entities.get(id);
                int entityKilled = killed.getOrDefault(id, 0);

                entitiesTag.putString(tagIndex, id.toString());
                countTag.putInt(tagIndex, entityCount);
                killedTag.putInt(tagIndex, entityKilled);

                index++;
            }
            outTag.put(TARGET_ENTITIES_NBT, entitiesTag);
            outTag.put(TARGET_COUNT_NBT, countTag);
            outTag.put(TARGET_KILLED_NBT, killedTag);
        }

        outTag.putBoolean(SPAWNED_NBT, spawned);

        if (dimension != null)
            outTag.putString(DIMENSION_NBT, dimension.toString());

        if (structure != null)
            outTag.putLong(STRUCTURE_NBT, structure.asLong());

        return outTag;
    }

    @Override
    public void fromNbt(CompoundTag nbt) {
        CompoundTag entitiesTag = (CompoundTag) nbt.get(TARGET_ENTITIES_NBT);
        CompoundTag countTag = (CompoundTag) nbt.get(TARGET_COUNT_NBT);
        CompoundTag killedTag = (CompoundTag) nbt.get(TARGET_KILLED_NBT);

        entities = new HashMap<>();

        structure = nbt.contains(STRUCTURE_NBT) ? BlockPos.of(nbt.getLong(STRUCTURE_NBT)) : null;
        dimension = ResourceLocation.tryParse(nbt.getString(DIMENSION_NBT));
        spawned = nbt.contains(SPAWNED_NBT) && nbt.getBoolean(SPAWNED_NBT);

        if (dimension == null)
            dimension = ScrollHelper.FALLBACK_DIMENSION;

        if (entitiesTag != null && entitiesTag.size() > 0 && countTag != null) {
            for (int i = 0; i < entitiesTag.size(); i++) {
                // read data from the tags at specified index
                String tagIndex = String.valueOf(i);
                ResourceLocation id = ResourceLocation.tryParse(entitiesTag.getString(tagIndex));
                if (id == null)
                    continue;

                int entityCount = countTag.getInt(tagIndex);
                int entityKilled = killedTag != null ? killedTag.getInt(tagIndex) : 0;
                entities.put(id, entityCount);
                killed.put(id, entityKilled);
            }
        }
    }

    public void addTarget(ResourceLocation entity, int count) {
        entities.put(entity, count);
    }

    public void setDimension(ResourceLocation dimension) {
        this.dimension = dimension;
    }

    public void setStructure(BlockPos structure) {
        this.structure = structure;
    }

    public Quest getQuest() {
        return quest;
    }

    public Map<ResourceLocation, Integer> getEntities() {
        return entities;
    }

    public Map<ResourceLocation, Integer> getKilled() {
        return killed;
    }

    public Map<ResourceLocation, String> getNames() {
        return names;
    }

    public Map<ResourceLocation, Boolean> getSatisfied() {
        return satisfied;
    }

    public BlockPos getStructure() {
        return structure;
    }

    public boolean isSatisfied() {
        if (entities.isEmpty())
            return true;

        return entities.size() == satisfied.size() && getSatisfied().values().stream().allMatch(r -> r);
    }

    public void forceKill(Mob entity) {
        ResourceLocation id = Registry.ENTITY_TYPE.getKey(entity.getType());
        Integer count = killed.getOrDefault(id, 0);
        killed.put(id, count + 1);
        quest.setDirty(true);
    }

    public void playerTick(Player player) {
        if (player.level.isClientSide || spawned || structure == null)
            return;

        double dist = PosHelper.getDistanceSquared(player.blockPosition(), structure);
        if (dist < BossPopulator.POPULATE_DISTANCE) {
            boolean result = BossPopulator.startEncounter(player, this);

            if (!result) {
                quest.abandon(player);
                return;
            }

            quest.setDirty(true);
            spawned = true;
        }
    }

    public void complete(Player player, AbstractVillager merchant) {
        BossPopulator.checkEncounter(player, this);
    }

    public void abandon(Player player) {
        entities.clear();
        BossPopulator.checkEncounter(player, this);
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
        List<String> tags = new ArrayList<>(entity.getTags());
        ResourceLocation id = Registry.ENTITY_TYPE.getKey(entity.getType());

        if (tags.contains(quest.getId())) {
            Integer count = killed.getOrDefault(id, 0);
            killed.put(id, count + 1);

            quest.setDirty(true);

            if (!(attacker instanceof ServerPlayer player))
                return;

            quest.update(player);
            BossPopulator.checkEncounter(player, this);
        }
    }
}
