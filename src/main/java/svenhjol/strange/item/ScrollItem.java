package svenhjol.strange.item;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.DimensionHelper;
import svenhjol.charm.base.item.CharmItem;
import svenhjol.strange.helper.ScrollHelper;
import svenhjol.strange.module.Scrolls;
import svenhjol.strange.scroll.JsonDefinition;
import svenhjol.strange.scroll.tag.Quest;

import javax.annotation.Nullable;
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

        if (!DimensionHelper.isDimension(world, new Identifier("overworld")) || player.isSneaking())
            return new TypedActionResult<>(ActionResult.FAIL, heldScroll);

        player.getItemCooldownManager().set(this, 10);

        if (world.isClient)
            return new TypedActionResult<>(ActionResult.PASS, heldScroll);

        boolean hasBeenOpened = hasBeenOpened(heldScroll);

        if (!hasBeenOpened) {
            // if the quest hasn't been populated yet, create it in the quest manager
            JsonDefinition definition = Scrolls.getRandomDefinition(tier, world.random);

            if (definition == null)
                return new TypedActionResult<>(ActionResult.FAIL, heldScroll);

            Scrolls.questManager.createQuest(heldScroll, (ServerPlayerEntity)player, definition);

            // tell the client to open the scroll
            playerShouldOpenScroll(player, heldScroll);
            return new TypedActionResult<>(ActionResult.SUCCESS, heldScroll);

        } else {

            Quest quest = getScrollQuest(heldScroll);
            if (quest == null) {

                // scroll has expired, remove it
                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                heldScroll.decrement(1);

            } else {

                // tell the client to open the scroll
                playerShouldOpenScroll(player, heldScroll);
            }
        }

        return super.use(world, player, hand);
    }

    private void playerShouldOpenScroll(PlayerEntity player, ItemStack scroll) {
        Quest quest = getScrollQuest(scroll);
        if (quest == null)
            return;

        PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
        data.writeCompoundTag(quest.toTag());
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Scrolls.MSG_CLIENT_OPEN_SCROLL, data);
    }

    public static boolean hasBeenOpened(ItemStack scroll) {
        return scroll.getOrCreateTag().contains(QUEST_TAG);
    }

    public static UUID getScrollMerchant(ItemStack scroll) {
        String string = scroll.getOrCreateTag().getString(MERCHANT_TAG);
        if (string.isEmpty())
            return ScrollHelper.ANY_UUID;

        return UUID.fromString(string);
    }

    @Nullable
    public static Quest getScrollQuest(ItemStack scroll) {
        String questId = scroll.getOrCreateTag().getString(QUEST_TAG);
        return Scrolls.questManager.getQuest(questId);
    }

    public static int getScrollRarity(ItemStack scroll) {
        return scroll.getOrCreateTag().getInt(RARITY_TAG);
    }

    public static void setScrollMerchant(ItemStack scroll, MerchantEntity merchant) {
        scroll.getOrCreateTag().putString(MERCHANT_TAG, merchant.getUuidAsString());
    }

    public static void setScrollQuest(ItemStack scroll, String questId) {
        scroll.getOrCreateTag().putString(QUEST_TAG, questId);
    }

    public static void setScrollName(ItemStack scroll, Text name) {
        scroll.setCustomName(name);
    }

    public static void setScrollRarity(ItemStack scroll, int rarity) {
        scroll.getOrCreateTag().putInt(RARITY_TAG, rarity);
    }
}
