package svenhjol.strange.module.quests.component;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.maps.MapDecoration.Type;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.MobHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.module.quests.IQuestComponent;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.helper.QuestDefinitionHelper;
import svenhjol.strange.module.quests.helper.QuestHelper;
import svenhjol.strange.module.stone_circles.StoneCircles;

import javax.annotation.Nullable;
import java.util.*;

public class BossComponent implements IQuestComponent {
    public static final String TARGETS_TAG = "targets";
    public static final String SUPPORT_TAG = "support";
    public static final String HEALTH_TAG = "health";
    public static final String EFFECTS_TAG = "effects";
    public static final String COUNT_TAG = "count";
    public static final String ENTITIES_TAG = "target_entities";
    public static final String TARGET_COUNT_TAG = "target_count";
    public static final String TARGET_KILLED_NBT = "target_killed";
    public static final String SPAWNED_TAG = "spawned";
    public static final String STRUCTURE_POS_TAG = "structure_pos";
    public static final String DIMENSION_TAG = "dimension";

    public static final int MAP_COLOR = 0x770000;
    public static final Type MAP_TAG = Type.RED_X;
    public static final int POPULATE_DISTANCE = 260;
    public static final int BOSS_EFFECT_DURATION = 99999;

    // All bosses are granted these effects. ID of effect as string, supports modded effects.
    public static final List<String> BOSS_EFFECTS;

    // If stone circles module not enabled then bosses appear at a pillager outpost.
    public static final ResourceLocation FALLBACK_STRUCTURE = new ResourceLocation("pillager_outpost");

    // Reference to the parent quest.
    private final Quest quest;

    // Stored in NBT.
    private @Nullable ResourceLocation dimension;
    private @Nullable BlockPos structurePos;
    private boolean spawned;
    private final Map<ResourceLocation, Integer> targets = new HashMap<>();
    private final Map<ResourceLocation, Integer> killed = new HashMap<>();

    // Dynamically generated and not stored in NBT.
    private final Map<ResourceLocation, Boolean> satisfied = new HashMap<>();

    public BossComponent(Quest quest) {
        this.quest = quest;
    }

    @Override
    public String getId() {
        return "boss";
    }

    @Override
    public boolean isEmpty() {
        return targets.isEmpty();
    }

    @Override
    public boolean isSatisfied(Player player) {
        if (isEmpty()) return true; // To bypass quests that don't have a boss component.
        return satisfied.size() == targets.size() && satisfied.values().stream().allMatch(b -> b);
    }

    public Map<ResourceLocation, Integer> getTargets() {
        return targets;
    }

    public Map<ResourceLocation, Integer> getKilled() {
        return killed;
    }

    @Override
    public void update(Player player) {
        satisfied.clear();

        targets.forEach((id, count) -> {
            var countKilled = killed.getOrDefault(id, 0);
            satisfied.put(id, countKilled >= count);
        });
    }

    @Override
    public void entityKilled(LivingEntity entity, Entity attacker) {
        var tags = entity.getTags();
        var id = Registry.ENTITY_TYPE.getKey(entity.getType());

        if (tags.contains(quest.getId())) {
            var count = killed.getOrDefault(id, 0);
            killed.put(id, count + 1);
            quest.setDirty();

            if (attacker instanceof Player player) {
                quest.update(player);
                // TODO: this used to call checkEncounter - what does this do?
            }
        }
    }

    @Override
    public boolean start(Player player) {
        if (player.level.isClientSide) return false;

        targets.clear();
        killed.clear();

        var serverPlayer = (ServerPlayer) player;
        var pos = serverPlayer.blockPosition();
        var random = serverPlayer.getRandom();
        var level = (ServerLevel) serverPlayer.getLevel();
        var definition = quest.getDefinition();

        // The boss definition has two sections:
        //   1. targets
        //   2. support
        //
        // "Targets" consists of one or more entities that must be killed to complete the quest.
        // "Support" consists of one of more entities that will be spawned along with targets but do not count toward completion.

        var bossDefinition = definition.getBoss();
        if (bossDefinition == null || bossDefinition.isEmpty()) {
            // No definition - no boss quest. Exit cleanly.
            return true;
        }

        var targetsMap = bossDefinition.getOrDefault(TARGETS_TAG, null);
        if (targetsMap == null || targetsMap.isEmpty()) {
            LogHelper.error(Strange.MOD_ID, getClass(), "Missing targets tag, cannot start quest.");
            return false;
        }

        var stoneCirclesEnabled = Strange.LOADER.isEnabled(StoneCircles.class);
        var structureId = stoneCirclesEnabled ? StoneCircles.STRUCTURE_ID : FALLBACK_STRUCTURE;
        var structureFeature = Registry.STRUCTURE_FEATURE.get(structureId);

        if (structureFeature == null) {
            LogHelper.error(Strange.MOD_ID, getClass(), "Invalid structure type, cannot start quest.");
            return false;
        }

        var blockPos = WorldHelper.addRandomOffset(pos, random, 250, 750);
        structurePos = level.findNearestMapFeature(structureFeature, blockPos, 500, false);

        if (structurePos == null) {
            LogHelper.error(Strange.MOD_ID, getClass(), "Could not find structure, cannot start quest.");
            return false;
        }

        // Check target validity, add to NBT.
        targetsMap.forEach((id, attributes) -> {
            var entityId = QuestDefinitionHelper.getEntityIdFromKey(id);
            if (entityId == null) return;

            var count = Integer.parseInt(attributes.getOrDefault(COUNT_TAG, "1"));
            targets.put(entityId, count);
        });

        // Set to the player's current dimension.
        dimension = DimensionHelper.getDimension(level);

        // Provide map to structure.
        provideMap(serverPlayer);

        return true;
    }

    @Override
    public void playerTick(Player player) {
        if (player.level.isClientSide) return;
        if (spawned) return;
        if (structurePos == null) return;
        if (!DimensionHelper.isDimension(player.level, dimension)) return;

        var serverPlayer = (ServerPlayer) player;
        var playerPos = serverPlayer.blockPosition();
        var level = serverPlayer.getLevel();
        var dist = WorldHelper.getDistanceSquared(playerPos, structurePos);

        if (dist > POPULATE_DISTANCE) return;

        var bossDefinition = quest.getDefinition().getBoss();
        var targetsMap = bossDefinition.get(TARGETS_TAG);
        var supportMap = bossDefinition.get(SUPPORT_TAG);

        if (targetsMap != null) {
            var result = trySpawnEntities(level, structurePos, targetsMap, true);
            if (!result) {
                quest.abandon(player);
                return;
            }
        }

        if (supportMap != null) {
            trySpawnEntities(level, structurePos, supportMap, false);
        }

        level.playSound(null, structurePos, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 1.0F, 1.0F);
        spawned = true;
    }

    private boolean trySpawnEntities(ServerLevel level, BlockPos pos, Map<String, Map<String, String>> entityMap, boolean isTarget) {
        var amplifier = Math.max(1, quest.getTier().ordinal() - 3);
        boolean didSpawn;

        for (Map.Entry<String, Map<String, String>> entry : entityMap.entrySet()) {
            var entityId = entry.getKey();
            var attributes = entry.getValue();

            var entityType = EntityType.byString(entityId).orElse(null);
            if (entityType == null) return false;

            var count = Integer.parseInt(attributes.getOrDefault(COUNT_TAG, "1"));

            for (int i = 0; i < count; i++) {
                var entity = entityType.create(level);
                if (!(entity instanceof Mob mob)) return false;

                var health = Integer.parseInt(attributes.getOrDefault(HEALTH_TAG, "20"));
                var effectsDefinition = attributes.getOrDefault(EFFECTS_TAG, "");
                List<String> effects = new ArrayList<>();

                if (effectsDefinition.length() > 0) {
                    if (effectsDefinition.contains(",")) {
                        effects.addAll(Arrays.asList(effectsDefinition.split(",")));
                    } else {
                        effects.add(effectsDefinition);
                    }
                }

                effects.addAll(BOSS_EFFECTS);

                didSpawn = MobHelper.spawnNearBlockPos(level, pos, mob, (m, p) -> {
                    m.setPersistenceRequired();

                    if (isTarget) {
                        m.addTag(quest.getId());
                    }

                    var healthAttribute = m.getAttribute(Attributes.MAX_HEALTH);
                    if (healthAttribute != null) {
                        healthAttribute.setBaseValue(health);
                    }
                    m.setHealth(health);

                    if (effects.size() > 0) {
                        effects.forEach(
                            e -> Registry.MOB_EFFECT.getOptional(new ResourceLocation(e)).ifPresent(
                                me -> m.addEffect(new MobEffectInstance(me, BOSS_EFFECT_DURATION, amplifier))));
                    }

                    LogHelper.info(Strange.MOD_ID, getClass(), "Spawned `" + entityId + "` at " + p);
                });

                if (!didSpawn) {
                    LogHelper.info(Strange.MOD_ID, getClass(), "Failed to spawn `" + entityId + "`");

                    if (isTarget) {
                        var resId = new ResourceLocation(entityId);
                        var entityCount = killed.getOrDefault(resId, 0);
                        killed.put(resId, entityCount + 1);
                        quest.setDirty();
                    }
                }
            }
        }

        return true;
    }

    @Override
    public CompoundTag save() {
        var out = new CompoundTag();
        var entitiesTag = new CompoundTag();
        var countTag = new CompoundTag();
        var killedTag = new CompoundTag();

        if (!targets.isEmpty()) {
            var i = 0;
            for (var id : targets.keySet()) {
                var index = Integer.toString(i);
                var targetCount = targets.get(id);
                var targetKilled = killed.getOrDefault(id, 0);

                // Write to tags at stringified index.
                entitiesTag.putString(index, id.toString());
                countTag.putInt(index, targetCount);
                killedTag.putInt(index, targetKilled);

                i++;
            }

            out.put(ENTITIES_TAG, entitiesTag);
            out.put(TARGET_COUNT_TAG, countTag);
            out.put(TARGET_KILLED_NBT, killedTag);
        }

        out.putBoolean(SPAWNED_TAG, spawned);

        if (dimension != null) {
            out.putString(DIMENSION_TAG, dimension.toString());
        }

        if (structurePos != null) {
            out.putLong(STRUCTURE_POS_TAG, structurePos.asLong());
        }

        return out;
    }

    @Override
    public void load(CompoundTag tag) {
        var entitiesTag = tag.getCompound(ENTITIES_TAG);
        var countTag = tag.getCompound(TARGET_COUNT_TAG);
        var killedTag = tag.getCompound(TARGET_KILLED_NBT);

        targets.clear();
        killed.clear();

        structurePos = tag.contains(STRUCTURE_POS_TAG) ? BlockPos.of(tag.getLong(STRUCTURE_POS_TAG)) : null;
        dimension = ResourceLocation.tryParse(tag.getString(DIMENSION_TAG));
        spawned = tag.getBoolean(SPAWNED_TAG);

        if (entitiesTag != null && entitiesTag.size() > 0 && countTag != null) {
            for (int i = 0; i < entitiesTag.size(); i++) {
                var tagIndex = String.valueOf(i);
                var id = ResourceLocation.tryParse(entitiesTag.getString(tagIndex));
                if (id == null) continue;

                var targetCount = countTag.getInt(tagIndex);
                var targetKilled = killedTag != null ? killedTag.getInt(tagIndex) : 0;

                targets.put(id, targetCount);
                killed.put(id, targetKilled);
            }
        }
    }

    @Override
    public void provideMap(ServerPlayer player) {
        QuestHelper.provideMap(player, quest, structurePos, MAP_TAG, MAP_COLOR);
    }

    static {
        BOSS_EFFECTS = Arrays.asList(
            "fire_resistance",
            "water_breathing"
        );
    }
}
