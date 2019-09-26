package svenhjol.strange.totems.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.ItemNBTHelper;
import svenhjol.strange.base.StrangeHelper;

import javax.annotation.Nullable;
import java.util.List;

public class TotemOfHoldingItem extends MesonItem
{
    private static final String MESSAGE = "message";
    private static final String ITEMS = "items";

    public TotemOfHoldingItem(MesonModule module)
    {
        super(module, "totem_of_holding", new Properties()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxStackSize(1)
        );
    }

    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return false;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    {
        ItemStack held = player.getHeldItem(hand);
        CompoundNBT items = getItems(held);
        StrangeHelper.destroyTotem(player, held);

        for (int i = 0; i < items.size(); i++) {
            INBT itemTag = items.get(String.valueOf(i));
            if (itemTag == null) continue; // TODO error about this
            ItemStack stack = ItemStack.read((CompoundNBT)itemTag);
            world.addEntity(new ItemEntity(world, player.posX, player.posY + 0.5D, player.posZ, stack));
        }

        return super.onItemRightClick(world, player, hand);
    }

    public static void setMessage(ItemStack stack, String message)
    {
        ItemNBTHelper.setString(stack, MESSAGE, message);
    }

    public static void setItems(ItemStack stack, CompoundNBT items)
    {
        ItemNBTHelper.setCompound(stack, ITEMS, items);
    }

    public static String getMessage(ItemStack stack)
    {
        return ItemNBTHelper.getString(stack, MESSAGE, "");
    }

    public static CompoundNBT getItems(ItemStack stack)
    {
        return ItemNBTHelper.getCompound(stack, ITEMS);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> strings, ITooltipFlag flag)
    {
        String message = getMessage(stack);
        CompoundNBT items = getItems(stack);

        if (!message.isEmpty()) {
            strings.add(new StringTextComponent(message));
        }

        if (!items.isEmpty()) {
            int size = items.size();
            String str = size == 1 ? "totem.strange.holding_item" : "totem.strange.holding_items";
            strings.add(new StringTextComponent(I18n.format(str, size)));
        }

        super.addInformation(stack, world, strings, flag);
    }


}
