package svenhjol.strange.scroll.populator;

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
import svenhjol.strange.module.Ruins;
import svenhjol.strange.scroll.JsonDefinition;
import svenhjol.strange.scroll.tag.ExploreTag;
import svenhjol.strange.scroll.tag.QuestTag;

import java.util.ArrayList;
import java.util.List;

public class ExplorePopulator extends Populator {

    public ExplorePopulator(ServerPlayerEntity player, QuestTag quest, JsonDefinition definition) {
        super(player, quest, definition);
    }

    @Override
    public void populate() {
        List<String> explore = definition.getExplore();

        // find the structure to explore
        BlockPos min = RunestoneHelper.addRandomOffset(pos, world.random, 1500);
        BlockPos max = RunestoneHelper.addRandomOffset(min, world.random, 1500);

        StructureFeature<?> structureFeature = Registry.STRUCTURE_FEATURE.get(Ruins.STRUCTURE_ID);
        if (structureFeature == null) {
            structureFeature = Registry.STRUCTURE_FEATURE.get(new Identifier("mineshaft"));
            if (structureFeature == null)
                fail("Could not find ruin or mineshaft");
        }

        BlockPos foundPos = world.locateStructure(structureFeature, max, 1000, true);
        if (foundPos == null)
            fail("Could not locate structure");


        // populate the items for the quest
        // TODO: handle fun names for items
        List<ItemStack> items = new ArrayList<>();

        for (String stackName : explore) {
            ItemStack stack = getItemFromKey(stackName);
            if (stack == null)
                continue;

            // set the quest tag of the stack to the quest ID so we can match it later
            stack.getOrCreateTag().putString(ExploreTag.QUEST, quest.getId());
            items.add(stack);
        }

        quest.getExplore().setItems(items);
        quest.getExplore().setDimension(DimensionHelper.getDimension(world));
        quest.getExplore().setStructurePos(foundPos);


        // give map to the location
        ItemStack map = MapHelper.getMap(world, foundPos, new TranslatableText(quest.getTitle()), MapIcon.Type.TARGET_X, 0x00FF00);
        PlayerHelper.addOrDropStack(player, map);
    }

    private void fail(String message) {
        // TODO: handle this fail condition
        throw new RuntimeException("Could not start exploration quest: " + message);
    }
}
