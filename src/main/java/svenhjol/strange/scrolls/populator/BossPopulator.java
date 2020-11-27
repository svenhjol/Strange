package svenhjol.strange.scrolls.populator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapIcon;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.StructureFeature;
import svenhjol.charm.Charm;
import svenhjol.charm.base.helper.*;
import svenhjol.strange.scrolls.Scrolls;
import svenhjol.strange.stonecircles.StoneCircles;
import svenhjol.strange.scrolls.JsonDefinition;
import svenhjol.strange.scrolls.tag.Boss;
import svenhjol.strange.scrolls.tag.Quest;

import java.util.*;

public class BossPopulator extends Populator {
    public static final String TARGETS = "targets";
    public static final String SETTINGS = "settings";
    public static final String SUPPORT = "support";
    public static final String COUNT = "count";
    public static final String HEALTH = "health";
    public static final String EFFECTS = "effects";

    public BossPopulator(ServerPlayerEntity player, Quest quest, JsonDefinition definition) {
        super(player, quest, definition);
    }

    @Override
    public void populate() {
        Map<String, Map<String, Map<String, String>>> boss = definition.getBoss();

        if (boss.isEmpty())
            return;

        BlockPos bossPos = PosHelper.addRandomOffset(pos, world.random, 250, 750);

        StructureFeature<?> structureFeature = Registry.STRUCTURE_FEATURE.get(StoneCircles.STRUCTURE_ID);
        if (structureFeature == null)
            fail("Could not find stone circle");

        // populate target entities
        BlockPos foundPos = world.locateStructure(structureFeature, bossPos, 500, false);
        if (foundPos == null)
            fail("Could not locate structure");

        Map<Identifier, Integer> entities = new HashMap<>();

        if (boss.containsKey(TARGETS)) {
            Map<String, Map<String, String>> targets = boss.get(TARGETS);

            for (String id : targets.keySet()) {
                Identifier entityId = getEntityIdFromKey(id);
                if (entityId == null)
                    continue;

                int count = 0;
                Map<String, String> targetProps = targets.get(id);
                if (targetProps.containsKey(COUNT))
                    count = getCountFromValue(targetProps.get(COUNT), 1, false);

                entities.put(entityId, Math.max(1, count));
            }
        }

        if (boss.containsKey(SETTINGS)) {
            // TODO: settings for boss encounters
        }

        quest.getBoss().setDimension(DimensionHelper.getDimension(world));
        quest.getBoss().setStructure(foundPos);
        entities.forEach(quest.getBoss()::addTarget);

        // give map to the location
        ItemStack map = MapHelper.getMap(world, foundPos, new TranslatableText(quest.getTitle()), MapIcon.Type.TARGET_POINT, 0x770000);
        PlayerHelper.addOrDropStack(player, map);
    }

    public static boolean startEncounter(PlayerEntity player, Boss tag) {
        Quest quest = tag.getQuest();
        ServerWorld world = (ServerWorld)player.world;
        BlockPos pos = tag.getStructure();

        if (pos == null)
            return false;

        JsonDefinition definition = Scrolls.AVAILABLE_SCROLLS.get(quest.getTier()).getOrDefault(quest.getDefinition(), null);
        if (definition == null)
            return false;

        Map<String, Map<String, Map<String, String>>> boss = definition.getBoss();

        // try and spawn the boss target entities
        if (boss.containsKey(TARGETS)) {
            if (!trySpawnEntities(world, pos, quest, boss.get(TARGETS), true))
                return false;
        }

        // try and spawn supporting entities
        if (boss.containsKey(SUPPORT))
            trySpawnEntities(world, pos, quest, boss.get(SUPPORT), false);

        world.playSound(null, pos, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 1.0F, 1.0F);
        return true;
    }

    public static void checkEncounter(PlayerEntity player, Boss tag) {
    }

    private void fail(String message) {
        throw new IllegalStateException("Could not start boss quest: " + message);
    }

    private static boolean trySpawnEntities(ServerWorld world, BlockPos pos, Quest quest, Map<String, Map<String, String>> entityDefinitions, boolean isBoss) {
        int effectDuration = 999999; // something that doesn't run out very quickly
        int effectAmplifier = Math.max(1, quest.getTier() - 3);
        boolean didAnySpawn = false;

        for (String id : entityDefinitions.keySet()) {
            Map<String, String> props = entityDefinitions.get(id);

            // try and get the type from the ID
            Optional<EntityType<?>> optionalType = EntityType.get(id);
            if (!optionalType.isPresent())
                return false;

            // get the count property and spawn this many mobs default to 1 if not set
            int count = Integer.parseInt(props.getOrDefault(COUNT, "1"));

            for (int n = 0; n < count; n++) {

                // try and create the entity from the type
                Entity entity = optionalType.get().create(world);
                if (!(entity instanceof MobEntity))
                    return false;

                MobEntity mobEntity = (MobEntity) entity;

                // get the health property, default to 20 hearts if not set
                int health = Integer.parseInt(props.getOrDefault(HEALTH, "20"));

                // parse effectsString into list of effects
                String effectsDef = props.getOrDefault(EFFECTS, "");
                final List<String> effects = new ArrayList<>();
                if (effectsDef.length() > 0) {
                    if (effectsDef.contains(",")) {
                        effects.addAll(Arrays.asList(effectsDef.split(",")));
                    } else {
                        effects.add(effectsDef);
                    }
                }

                // all entities start with fire resist and water breathing by default
                effects.add("fire_resistance");
                effects.add("water_breathing");

                // try and add mob to the world, if fail then record the mob as killed
                boolean didSpawn = MobHelper.spawnMobNearPos(world, pos, mobEntity, (mob, mobPos) -> {
                    // set properties on the entity
                    mob.setPersistent();

                    // if this entity should be a boss, set the quest ID tag on it
                    if (isBoss)
                        mob.addScoreboardTag(quest.getId());

                    // need to override this attribute on the entity to allow health values greater than maxhealth
                    EntityAttributeInstance healthAttribute = mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                    if (healthAttribute != null)
                        healthAttribute.setBaseValue(health);

                    mob.setHealth(health);

                    // apply status effects to the mob
                    if (effects.size() > 0) {
                        effects.forEach(effectName -> {
                            StatusEffect effect = Registry.STATUS_EFFECT.get(new Identifier(effectName));
                            if (effect == null)
                                return;

                            mob.addStatusEffect(new StatusEffectInstance(effect, effectDuration, effectAmplifier));
                        });
                    }

                    Charm.LOG.info("Spawned " + id + " at " + mobPos.toShortString());
                });

                if (didSpawn) {
                    didAnySpawn = true;
                } else {
                    Charm.LOG.info("Failed to spawn " + id);

                    // if a boss entity couldn't spawn, flag it as a kill
                    if (isBoss)
                        quest.getBoss().forceKill(mobEntity);
                }
            }
        }

        return didAnySpawn;
    }
}
