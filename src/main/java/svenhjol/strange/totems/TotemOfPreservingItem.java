package svenhjol.strange.totems;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.ItemNBTHelper;
import svenhjol.charm.base.item.CharmItem;

import javax.annotation.Nullable;
import java.util.List;

public class TotemOfPreservingItem extends CharmItem {
    public static final String MESSAGE_TAG = "message";
    public static final String ITEMS_TAG = "items";

    public TotemOfPreservingItem(CharmModule module) {
        super(module, "totem_of_preserving", new Item.Settings()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxCount(1));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack totem = user.getStackInHand(hand);
        CompoundTag items = getItems(totem);

        if (!items.isEmpty()) {
            TotemsHelper.destroy(user, totem);

            if (!world.isClient) {
                for (int i = 0; i < items.getSize(); i++) {
                    Tag tag = items.get(String.valueOf(i));
                    if (tag == null) {
                        Charm.LOG.warn("Item tag missing from totem");
                        continue;
                    }

                    ItemStack stack = ItemStack.fromTag((CompoundTag) tag);
                    BlockPos pos = user.getBlockPos();
                    ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY() + 0.5D, pos.getZ(), stack);
                    world.spawnEntity(itemEntity);
                }
            }
        }

        return super.use(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        String message = getMessage(stack);
        CompoundTag items = getItems(stack);

        if (!message.isEmpty())
            tooltip.add(new LiteralText(message));

        if (!items.isEmpty()) {
            int size = items.getSize();
            String str = size == 1 ? "totem.strange.preserving.item" : "totem.strange.preserving.items";
            tooltip.add(new LiteralText(I18n.translate(str, size)));
        }

        super.appendTooltip(stack, world, tooltip, context);
    }

    public static void setMessage(ItemStack totem, String message) {
        ItemNBTHelper.setString(totem, MESSAGE_TAG, message);
    }

    public static void setItems(ItemStack totem, CompoundTag items) {
        ItemNBTHelper.setCompound(totem, ITEMS_TAG, items);
    }

    public static String getMessage(ItemStack totem) {
        return ItemNBTHelper.getString(totem, MESSAGE_TAG, "");
    }

    public static CompoundTag getItems(ItemStack totem) {
        return ItemNBTHelper.getCompound(totem, ITEMS_TAG);
    }
}
