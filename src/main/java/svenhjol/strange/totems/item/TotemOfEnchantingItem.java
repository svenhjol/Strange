package svenhjol.strange.totems.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.ItemNBTHelper;
import svenhjol.strange.base.TotemHelper;

import javax.annotation.Nullable;
import java.util.List;

public class TotemOfEnchantingItem extends MesonItem
{
    private static final String XP = "xp";

    public TotemOfEnchantingItem(MesonModule module)
    {
        super(module, "totem_of_enchanting", new Properties()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxStackSize(1)
        );
    }

    @Override
    public boolean hasEffect(ItemStack stack)
    {
        return TotemOfEnchantingItem.getXp(stack) > 0;
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
        if (player.isSneaking()) {
            int xp = player.experienceTotal;
            addXp(held, xp);
            player.giveExperiencePoints(-xp);

            if (world.isRemote) {
                TotemHelper.effectActivateTotem(player.getPosition());
            }

            return new ActionResult<>(ActionResultType.SUCCESS, held);
        } else {
            int xp = getXp(held);
            if (xp > 0) {
                player.giveExperiencePoints(xp);
                TotemHelper.destroy(player, held);
                return new ActionResult<>(ActionResultType.SUCCESS, held);
            }
        }

        return super.onItemRightClick(world, player, hand);
    }

    public static void addXp(ItemStack stack, int amount)
    {
        int current = ItemNBTHelper.getInt(stack, XP, 0);
        ItemNBTHelper.setInt(stack, XP, current + amount);
    }

    public static int getXp(ItemStack stack)
    {
        return ItemNBTHelper.getInt(stack, XP, 0);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> strings, ITooltipFlag flag)
    {
        strings.add(new StringTextComponent(I18n.format("totem.strange.enchanting.contains", getXp(stack))));
        super.addInformation(stack, world, strings, flag);
    }
}
