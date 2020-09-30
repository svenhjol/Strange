package svenhjol.strange.scroll.populator;

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
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.StructureFeature;
import svenhjol.meson.Meson;
import svenhjol.meson.helper.DimensionHelper;
import svenhjol.meson.helper.MapHelper;
import svenhjol.meson.helper.PlayerHelper;
import svenhjol.strange.helper.RunestoneHelper;
import svenhjol.strange.helper.ScrollHelper;
import svenhjol.strange.module.Scrolls;
import svenhjol.strange.module.StoneCircles;
import svenhjol.strange.scroll.JsonDefinition;
import svenhjol.strange.scroll.tag.BossTag;
import svenhjol.strange.scroll.tag.QuestTag;

import java.util.*;

public class BossPopulator extends Populator {
    public static final String TARGETS = "targets";
    public static final String SETTINGS = "settings";
    public static final String ENTITIES = "entities";
    public static final String COUNT = "count";
    public static final String HEALTH = "health";
    public static final String EFFECTS = "effects";

    public BossPopulator(ServerPlayerEntity player, QuestTag quest, JsonDefinition definition) {
        super(player, quest, definition);
    }

    @Override
    public void populate() {
        Map<String, Map<String, Map<String, String>>> boss = definition.getBoss();

        if (boss.isEmpty())
            return;

        BlockPos min = RunestoneHelper.addRandomOffset(pos, world.random, 250);
        BlockPos max = RunestoneHelper.addRandomOffset(min, world.random, 250);

        StructureFeature<?> structureFeature = Registry.STRUCTURE_FEATURE.get(StoneCircles.STRUCTURE_ID);
        if (structureFeature == null)
            fail("Could not find stone circle");

        // populate target entities
        BlockPos foundPos = world.locateStructure(structureFeature, max, 500, false);
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
                    count = Integer.parseInt(targetProps.get(COUNT));

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

    public static void startEncounter(PlayerEntity player, BossTag tag) {
        QuestTag quest = tag.getQuest();
        World world = player.world;
        ServerWorld serverWorld = (ServerWorld)world;
        BlockPos pos = tag.getStructure();

        if (pos == null)
            return; // TODO: handle scroll errors

        JsonDefinition definition = Scrolls.AVAILABLE_SCROLLS.get(quest.getTier()).getOrDefault(quest.getDefinition(), null);
        if (definition == null)
            return; // TODO: handle scroll errors

        int effectDuration = 10000; // something that doesn't run out very quickly
        int effectAmplifier = Math.max(1, quest.getTier() - 3);
        Map<String, Map<String, Map<String, String>>> boss = definition.getBoss();

        // try and spawn the boss target entities
        if (boss.containsKey(TARGETS)) {
            boolean didAnySpawn = false;
            Map<String, Map<String, String>> targets = boss.get(TARGETS);

            for (String id : targets.keySet()) {
                Map<String, String> props = targets.get(id);

                // try and get the type from the ID
                Optional<EntityType<?>> optionalType = EntityType.get(id);
                if (!optionalType.isPresent())
                    return;

                // get the count property and spawn this many mobs default to 1 if not set
                int count = Integer.parseInt(props.getOrDefault(COUNT, "1"));

                for (int n = 0; n < count; n++) {

                    // try and create the entity from the type
                    Entity entity = optionalType.get().create(world);
                    if (!(entity instanceof MobEntity))
                        return;

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

                    effects.add("fire_resistance");
                    effects.add("water_breathing");


                    // try and add mob to the world, if fail then record the mob as killed
                    boolean didSpawn = ScrollHelper.spawnMobNearPos(serverWorld, pos, mobEntity, (mob, mobPos) -> {
                        // set properties on the entity
                        mob.setPersistent();
                        mob.addScoreboardTag(quest.getId()); // this flags the mob as a target

                        // need to override this attribute on the entity to allow health values greater than maxhealth
                        EntityAttributeInstance healthAttribute = mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                        if (healthAttribute != null)
                            healthAttribute.setBaseValue(health);

                        mob.setHealth(health);

                        if (effects.size() > 0) {
                            effects.forEach(effectName -> {
                                StatusEffect effect = Registry.STATUS_EFFECT.get(new Identifier(effectName));
                                if (effect == null)
                                    return;

                                mob.addStatusEffect(new StatusEffectInstance(effect, effectDuration, effectAmplifier));
                            });
                        }

                        Meson.LOG.info("Spawned " + id + " at " + mobPos.toShortString());
                    });

                    if (didSpawn) {
                        didAnySpawn = true;
                    } else {
                        Meson.LOG.info("Failed to spawn " + id);
                        quest.getBoss().forceKill(mobEntity);
                    }
                }
            }

            if (!didAnySpawn)
                return; // TODO: handle scroll errors

            ScrollHelper.stormyWeather(serverWorld);
        }
    }

    public static void checkEncounter(PlayerEntity player, BossTag tag) {
        if (player.world.isClient)
            return;

        if (tag.isSatisfied())
            ScrollHelper.clearWeather((ServerWorld)player.world);
    }

    private void fail(String message) {
        // TODO: handle fail conditions
        throw new RuntimeException("Could not start boss quest: " + message);
    }
}
