package svenhjol.strange.item;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
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

import javax.annotation.Nullable;
import java.util.UUID;

public class ScrollItem extends MesonItem {
    private static final String QUEST_TAG = "quest";
    private static final String RARITY_TAG = "rarity";
    private static final String MERCHANT_TAG = "merchant";

    private final int tier;

    public ScrollItem(MesonModule module, int tier, String name) {
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
        int rarity = getScrollRarity(heldScroll);
        UUID merchant = getScrollMerchant(heldScroll);

        if (!DimensionHelper.isDimension(world, new Identifier("overworld")) || player.isSneaking())
            return new TypedActionResult<>(ActionResult.FAIL, heldScroll);

        player.getItemCooldownManager().set(this, 40);

        if (world.isClient)
            return new TypedActionResult<>(ActionResult.PASS, heldScroll);

        if (!hasBeenPopulated(heldScroll)) {
            ScrollDefinition definition = Scrolls.getRandomDefinition(tier, world.random);

            if (definition == null)
                return new TypedActionResult<>(ActionResult.FAIL, heldScroll);

            ScrollQuest quest = ScrollQuestCreator.create(player, definition, rarity, merchant);
            setScrollQuest(heldScroll, quest);
            heldScroll.setCustomName(new TranslatableText(quest.getTitle()));
        }

        if (hasBeenPopulated(heldScroll)) {
            // tell the client to open the scroll
            playerShouldOpenScroll(player, heldScroll);
            return new TypedActionResult<>(ActionResult.SUCCESS, heldScroll);
        }

        return super.use(world, player, hand);
    }

    private void playerShouldOpenScroll(PlayerEntity player, ItemStack scroll) {
        ScrollQuest quest = getScrollQuest(scroll);
        if (quest == null)
            return;

        PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
        data.writeCompoundTag(quest.toTag());
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Scrolls.MSG_CLIENT_OPEN_SCROLL, data);
    }

    public static boolean hasBeenPopulated(ItemStack scroll) {
        return scroll.getOrCreateTag().contains(QUEST_TAG);
    }

    public static UUID getScrollMerchant(ItemStack scroll) {
        return UUID.fromString(scroll.getOrCreateTag().getString(MERCHANT_TAG));
    }

    @Nullable
    public static ScrollQuest getScrollQuest(ItemStack scroll) {
        if (!hasBeenPopulated(scroll))
            return null;

        ScrollQuest quest = new ScrollQuest();
        CompoundTag tag = scroll.getOrCreateTag().getCompound(QUEST_TAG);

        if (tag == null)
            return null;

        quest.fromTag(tag);
        return quest;
    }

    public static int getScrollRarity(ItemStack scroll) {
        return scroll.getOrCreateTag().getInt(RARITY_TAG);
    }

    public static void setScrollMerchant(ItemStack scroll, UUID merchant) {
        scroll.getOrCreateTag().putString(MERCHANT_TAG, merchant.toString());
    }

    public static void setScrollQuest(ItemStack scroll, ScrollQuest quest) {
        scroll.getOrCreateTag().put(QUEST_TAG, quest.toTag());
    }

    public static void setScrollRarity(ItemStack scroll, int rarity) {
        scroll.getOrCreateTag().putInt(RARITY_TAG, rarity);
    }
}
