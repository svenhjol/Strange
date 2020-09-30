package svenhjol.strange.scroll.populator;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapIcon;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.StructureFeature;
import svenhjol.meson.helper.DimensionHelper;
import svenhjol.meson.helper.MapHelper;
import svenhjol.meson.helper.PlayerHelper;
import svenhjol.strange.helper.RunestoneHelper;
import svenhjol.strange.module.Scrolls;
import svenhjol.strange.module.StoneCircles;
import svenhjol.strange.scroll.JsonDefinition;
import svenhjol.strange.scroll.tag.BossTag;
import svenhjol.strange.scroll.tag.QuestTag;

import java.util.HashMap;
import java.util.Map;

public class BossPopulator extends Populator {
    public static final String TARGETS = "targets";
    public static final String SETTINGS = "settings";
    public static final String ENTITIES = "entities";
    public static final String COUNT = "count";

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
        ItemStack map = MapHelper.getMap(world, foundPos, new TranslatableText(quest.getTitle()), MapIcon.Type.RED_MARKER, 0x770000);
        PlayerHelper.addOrDropStack(player, map);
    }

    public static void startEncounter(PlayerEntity player, BossTag tag) {
        QuestTag quest = tag.getQuest();
        JsonDefinition definition = Scrolls.AVAILABLE_SCROLLS.get(quest.getTier()).getOrDefault(quest.getDefinition(), null);
//        if (definition == null)
//            throw new RuntimeException("Could not load definition for quest: " + quest.getDefinition());

    }

    private void fail(String message) {
        // TODO: handle this fail condition
        throw new RuntimeException("Could not start boss quest: " + message);
    }
}
