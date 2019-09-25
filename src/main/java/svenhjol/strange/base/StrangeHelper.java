package svenhjol.strange.base;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;

public class StrangeHelper
{
    public static void destroyTotem(PlayerEntity player, ItemStack totem)
    {
        totem.shrink(1);
        player.playSound(SoundEvents.ITEM_TOTEM_USE, 0.8F, 1.0F);
    }

    public static int damageTotem(PlayerEntity player, ItemStack totem, int amount)
    {
        int damage = totem.getDamage() + amount;
        totem.setDamage(damage);
        player.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.25F);
        return damage;
    }
}
