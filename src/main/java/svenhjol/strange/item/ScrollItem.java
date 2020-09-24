package svenhjol.strange.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.world.World;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.DimensionHelper;
import svenhjol.meson.item.MesonItem;
import svenhjol.strange.module.Scrolls;
import svenhjol.strange.scroll.ScrollDefinition;
import svenhjol.strange.scroll.ScrollQuest;
import svenhjol.strange.scroll.ScrollQuestCreator;

public class ScrollItem extends MesonItem {
    private static final String QUEST_TAG = "quest";
    private static final String RARITY_TAG = "rarity";

    private final int tier;

    public ScrollItem(MesonModule module, int tier) {
        super(module, "scroll_tier_" + tier, new Item.Settings()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxCount(1)
        );

        this.tier = tier;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack scrollItem = user.getStackInHand(hand);
        int rarity = getScrollRarity(scrollItem);

        if (!DimensionHelper.isDimension(world, new Identifier("overworld")) || user.isSneaking())
            return new TypedActionResult<>(ActionResult.FAIL, scrollItem);

        user.getItemCooldownManager().set(this, 40);

        if (world.isClient)
            return new TypedActionResult<>(ActionResult.PASS, scrollItem);

        if (!hasBeenPopulated(scrollItem)) {
            ScrollDefinition definition = Scrolls.getRandomDefinition(tier, world.random);

            if (definition == null)
                return new TypedActionResult<>(ActionResult.FAIL, scrollItem);

            ScrollQuest quest = ScrollQuestCreator.create(user, definition, rarity);
            setScrollQuest(scrollItem, quest);
            scrollItem.setCustomName(new TranslatableText(quest.getTitle()));

            // TODO: send message to client to open scroll

            return new TypedActionResult<>(ActionResult.SUCCESS, scrollItem);
        }

        return super.use(world, user, hand);
    }

    public static boolean hasBeenPopulated(ItemStack scroll) {
        return scroll.getOrCreateTag().contains(QUEST_TAG);
    }

    public static int getScrollRarity(ItemStack scroll) {
        return scroll.getOrCreateTag().getInt(RARITY_TAG);
    }

    public static void setScrollRarity(ItemStack scroll, int rarity) {
        scroll.getOrCreateTag().putInt(RARITY_TAG, rarity);
    }

    public static void setScrollQuest(ItemStack scroll, ScrollQuest quest) {
        scroll.getOrCreateTag().put(QUEST_TAG, quest.toTag());
    }
}
