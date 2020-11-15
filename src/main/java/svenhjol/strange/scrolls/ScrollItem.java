package svenhjol.strange.scrolls;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.DimensionHelper;
import svenhjol.charm.base.helper.PlayerHelper;
import svenhjol.charm.base.item.CharmItem;
import svenhjol.strange.scrolls.tag.Quest;

import java.util.Optional;
import java.util.UUID;

public class ScrollItem extends CharmItem {
    private static final String QUEST_TAG = "quest";
    private static final String RARITY_TAG = "rarity";
    private static final String MERCHANT_TAG = "merchant";

    private final int tier;

    public ScrollItem(CharmModule module, int tier, String name) {
        super(module, name, new Item.Settings()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxCount(1)
        );

        this.tier = tier;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack heldScroll = player.getStackInHand(hand);

        Optional<QuestManager> optionalQuestManager = Scrolls.getQuestManager();
        if (!optionalQuestManager.isPresent())
            return new TypedActionResult<>(ActionResult.FAIL, heldScroll);

        QuestManager questManager = optionalQuestManager.get();

        if (!DimensionHelper.isDimension(world, new Identifier("overworld")) || player.isSneaking())
            return new TypedActionResult<>(ActionResult.FAIL, heldScroll);

        player.getItemCooldownManager().set(this, 10);

        if (world.isClient)
            return new TypedActionResult<>(ActionResult.PASS, heldScroll);

        boolean hasBeenOpened = hasBeenOpened(heldScroll);

        if (!hasBeenOpened) {
            // if the quest hasn't been populated yet, create it in the quest manager
            JsonDefinition definition = Scrolls.getRandomDefinition(tier, world.random);
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;

            if (definition == null)
                return new TypedActionResult<>(ActionResult.FAIL, heldScroll);

            if (!questManager.checkPlayerCanStartQuest(serverPlayer))
                return new TypedActionResult<>(ActionResult.FAIL, heldScroll);


            UUID seller = getScrollMerchant(heldScroll);
            int rarity = Math.min(1, getScrollRarity(heldScroll));

            Quest quest = questManager.createQuest(serverPlayer, definition, rarity, seller);
            setScrollQuest(heldScroll, quest);

            // tell the client to open the scroll
            questManager.openScroll(player, quest);
            return new TypedActionResult<>(ActionResult.SUCCESS, heldScroll);

        } else {

            Optional<Quest> quest = getScrollQuest(heldScroll);
            if (quest.isPresent()) {

                // tell the client to open the scroll
                questManager.openScroll(player, quest.get());

            } else {

                // scroll has expired, remove it
                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                heldScroll.decrement(1);
            }
        }

        return super.use(world, player, hand);
    }

    public static boolean hasBeenOpened(ItemStack scroll) {
        return scroll.getOrCreateTag().contains(QUEST_TAG);
    }

    public static void giveScrollToPlayer(Quest quest, PlayerEntity player) {
        // allows quest objectives to update themselves
        quest.update(player);

        // create a new scroll for this quest and give it to the player
        ItemStack scroll = new ItemStack(Scrolls.SCROLL_TIERS.get(quest.getTier()));
        ScrollItem.setScrollQuest(scroll, quest);
        PlayerHelper.addOrDropStack(player, scroll);
    }

    public static UUID getScrollMerchant(ItemStack scroll) {
        String string = scroll.getOrCreateTag().getString(MERCHANT_TAG);
        if (string.isEmpty())
            return ScrollHelper.ANY_UUID;

        return UUID.fromString(string);
    }

    public static Optional<Quest> getScrollQuest(ItemStack scroll) {
        String questId = scroll.getOrCreateTag().getString(QUEST_TAG);
        return Scrolls.questManager.getQuest(questId);
    }

    public static int getScrollRarity(ItemStack scroll) {
        return scroll.getOrCreateTag().getInt(RARITY_TAG);
    }

    public static void setScrollMerchant(ItemStack scroll, MerchantEntity merchant) {
        scroll.getOrCreateTag().putString(MERCHANT_TAG, merchant.getUuidAsString());
    }

    public static void setScrollQuest(ItemStack scroll, Quest quest) {
        scroll.getOrCreateTag().putString(QUEST_TAG, quest.getId());
        setScrollName(scroll, quest);
    }

    public static void setScrollName(ItemStack scroll, Quest quest) {
        scroll.setCustomName(new TranslatableText(quest.getTitle()));
    }

    public static void setScrollRarity(ItemStack scroll, int rarity) {
        scroll.getOrCreateTag().putInt(RARITY_TAG, rarity);
    }
}
