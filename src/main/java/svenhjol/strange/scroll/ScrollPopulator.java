package svenhjol.strange.scroll;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.strange.item.ScrollItem;
import svenhjol.strange.scroll.populator.GatherGenerator;
import svenhjol.strange.scroll.populator.LangPopulator;
import svenhjol.strange.scroll.populator.Populator;
import svenhjol.strange.scroll.populator.RewardPopulator;
import svenhjol.strange.scroll.tag.QuestTag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ScrollPopulator {
    public static void populate(ItemStack scroll, PlayerEntity player, JsonDefinition definition) {
        UUID merchant = ScrollItem.getScrollMerchant(scroll);
        int rarity = ScrollItem.getScrollRarity(scroll);

        World world = player.world;
        BlockPos pos = player.getBlockPos();
        QuestTag quest = new QuestTag(merchant, rarity);

        List<Populator> populators = new ArrayList<>(Arrays.asList(
            new LangPopulator(world, pos, quest, definition),
            new RewardPopulator(world, pos, quest, definition),
            new GatherGenerator(world, pos, quest, definition)
        ));

        populators.forEach(Populator::populate);
        ScrollItem.setScrollQuest(scroll, quest);
        scroll.setCustomName(new TranslatableText(quest.getTitle()));
    }
}
