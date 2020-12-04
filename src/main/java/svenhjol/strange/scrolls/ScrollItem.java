package svenhjol.strange.scrolls;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.PlayerHelper;
import svenhjol.charm.base.item.CharmItem;
import svenhjol.strange.base.helper.NetworkHelper;
import svenhjol.strange.scrolls.tag.Quest;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class ScrollItem extends CharmItem {
    private static final String QUEST_TAG = "quest";
    private static final String RARITY_TAG = "rarity";
    private static final String MERCHANT_TAG = "merchant";
    private static final String OWNER_TAG = "owner";

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
        ItemStack held = player.getStackInHand(hand);

        if (world.isClient)
            return new TypedActionResult<>(ActionResult.PASS, held);

        Optional<QuestManager> optionalQuestManager = Scrolls.getQuestManager();
        if (!optionalQuestManager.isPresent())
            return new TypedActionResult<>(ActionResult.FAIL, held);

        QuestManager questManager = optionalQuestManager.get();

        player.getItemCooldownManager().set(this, 10);
        boolean hasBeenOpened = hasBeenOpened(held);

        // if the quest hasn't been populated yet, create it in the quest manager
        if (!hasBeenOpened) {
            JsonDefinition definition = Scrolls.getRandomDefinition(tier, world, world.random);
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;

            if (definition == null)
                return destroyScroll(world, player, held);

            if (player.isSneaking())
                return new TypedActionResult<>(ActionResult.FAIL, held);

            if (!questManager.checkPlayerCanStartQuest(serverPlayer))
                return new TypedActionResult<>(ActionResult.FAIL, held);

            UUID seller = getScrollMerchant(held);
            int rarity = Math.min(1, getScrollRarity(held));

            // if quest fails to generate then destroy it here
            Quest quest = questManager.createQuest(serverPlayer, definition, rarity, seller);
            if (quest == null) {

                // if the scroll was purchased, refund the emeralds with stack count equal to the tier
                if (!seller.equals(ScrollsHelper.ANY_UUID))
                    PlayerHelper.addOrDropStack(player, new ItemStack(Items.EMERALD, tier));

                return destroyScroll(world, player, held);
            }

//            questManager.sendToast(player, quest, QuestToastType.General, "event.strange.quests.accepted");
            setScrollQuest(held, quest);
            setScrollOwner(held, player);


            // tell the client to open the scroll
            ScrollsServer.sendPlayerOpenScrollPacket(serverPlayer, quest);
            return new TypedActionResult<>(ActionResult.SUCCESS, held);

        } else {

            String questId = getScrollQuest(held);
            if (questId == null)
                return destroyScroll(world, player, held);

            // check if it belongs to current player
            UUID owner = getScrollOwner(held);
            if (owner != null && !player.getUuid().equals(owner) && !player.isCreative()) {
                player.sendMessage(new TranslatableText("gui.strange.scrolls.wrong_owner"), true);
                return TypedActionResult.fail(held);
            }

            // try and open the quest, or destroy it if it's no longer valid
            Optional<Quest> optionalQuest = questManager.getQuest(questId);

            if (optionalQuest.isPresent()) {

                // if there's no owner, then set it to current player
                if (owner == null)
                    claimOwnership(held, player);

                ScrollsServer.sendPlayerOpenScrollPacket((ServerPlayerEntity)player, optionalQuest.get());
            } else {
                // scroll has expired, remove it
                return destroyScroll(world, player, held);
            }
        }

        return super.use(world, player, hand);
    }

    public static boolean hasBeenOpened(ItemStack scroll) {
        return scroll.getOrCreateTag().contains(QUEST_TAG);
    }

    public static void claimOwnership(ItemStack scroll, PlayerEntity player) {
        player.sendMessage(new TranslatableText("gui.strange.scrolls.claim_ownership"), true);
        String questId = ScrollItem.getScrollQuest(scroll);

        Scrolls.getQuestManager().flatMap(manager
            -> manager.getQuest(questId)).ifPresent(quest
                -> quest.setOwner(player.getUuid()));

        ScrollItem.setScrollOwner(scroll, player);
    }

    public static void giveScrollToPlayer(Quest quest, PlayerEntity player) {
        // allows quest objectives to update themselves
        quest.update(player);

        // create a new scroll for this quest and give it to the player
        ItemStack scroll = new ItemStack(Scrolls.SCROLL_TIERS.get(quest.getTier()));
        quest.setOwner(player.getUuid());
        ScrollItem.setScrollQuest(scroll, quest);
        PlayerHelper.addOrDropStack(player, scroll);
    }

    public static UUID getScrollMerchant(ItemStack scroll) {
        String string = scroll.getOrCreateTag().getString(MERCHANT_TAG);
        if (string.isEmpty())
            return ScrollsHelper.ANY_UUID;

        return UUID.fromString(string);
    }

    public static String getScrollQuest(ItemStack scroll) {
        return scroll.getOrCreateTag().getString(QUEST_TAG);
    }

    @Nullable
    public static UUID getScrollOwner(ItemStack scroll) {
        String string = scroll.getOrCreateTag().getString(OWNER_TAG);
        if (string.isEmpty())
            return null;

        return UUID.fromString(string);
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

    public static void setScrollOwner(ItemStack scroll, PlayerEntity player) {
        scroll.getOrCreateTag().putString(OWNER_TAG, player.getUuidAsString());
    }

    public static void setScrollRarity(ItemStack scroll, int rarity) {
        scroll.getOrCreateTag().putInt(RARITY_TAG, rarity);
    }

    public TypedActionResult<ItemStack> destroyScroll(World world, PlayerEntity player, ItemStack scroll) {
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.PLAYERS, 1.0F, 1.0F);
        scroll.decrement(1);

        NetworkHelper.sendEmptyPacketToClient((ServerPlayerEntity)player, Scrolls.MSG_CLIENT_DESTROY_SCROLL);
        return new TypedActionResult<>(ActionResult.FAIL, scroll);
    }
}
