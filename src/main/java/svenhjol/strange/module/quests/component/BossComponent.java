package svenhjol.strange.module.quests.component;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.maps.MapDecoration.Type;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.module.quests.IQuestComponent;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.helper.QuestDefinitionHelper;
import svenhjol.strange.module.quests.helper.QuestHelper;
import svenhjol.strange.module.stone_circles.StoneCircles;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BossComponent implements IQuestComponent {
    public static final String TARGETS_TAG = "targets";
    public static final String SETTINGS_TAG = "settings";
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
    private final Map<ResourceLocation, String> names = new HashMap<>();

    public BossComponent(Quest quest) {
        this.quest = quest;
    }

    @Override
    public String getId() {
        return "boss";
    }

    @Override
    public boolean isEmpty() {
        return false;
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
    public void update(Player player) {

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
