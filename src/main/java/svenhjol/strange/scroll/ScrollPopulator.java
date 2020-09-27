package svenhjol.strange.scroll;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import svenhjol.strange.item.ScrollItem;
import svenhjol.strange.scroll.populator.*;
import svenhjol.strange.scroll.tag.QuestTag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ScrollPopulator {
    public static void populate(ItemStack scroll, PlayerEntity player, JsonDefinition definition) {
        UUID merchant = ScrollItem.getScrollMerchant(scroll);
        int rarity = ScrollItem.getScrollRarity(scroll);
        QuestTag quest = new QuestTag(merchant, rarity);

        List<Populator> populators = new ArrayList<>(Arrays.asList(
            new LangPopulator(player, quest, definition),
            new RewardPopulator(player, quest, definition),
            new GatherPopulator(player, quest, definition),
            new HuntPopulator(player, quest, definition)
        ));

        populators.forEach(Populator::populate);
        ScrollItem.setScrollQuest(scroll, quest);
        scroll.setCustomName(new TranslatableText(quest.getTitle()));
    }
}
