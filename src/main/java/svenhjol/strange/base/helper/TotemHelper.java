package svenhjol.strange.base.helper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import svenhjol.meson.helper.ClientHelper;

public class TotemHelper {
    @SuppressWarnings("UnusedReturnValue")
    public static boolean damageOrDestroy(PlayerEntity player, ItemStack totem, int amount) {
        int damage = damage(player, totem, amount);
        if (damage > totem.getMaxDamage()) {
            destroy(player, totem);
            return true;
        }
        return false;
    }

    public static boolean destroy(PlayerEntity player, ItemStack totem) {
        if (player.isSpectator() || player.isCreative()) return false;

        totem.shrink(1);

        if (player.world.isRemote) effectDestroyTotem(player.getPosition());
        return true;
    }

    public static int damage(PlayerEntity player, ItemStack totem, int amount) {
        if (player.isSpectator() || player.isCreative()) return 0;

        int damage = totem.getDamage() + amount;
        totem.setDamage(damage);

        if (player.world.isRemote) effectDamageTotem(player.getPosition());

        return damage;
    }

    @OnlyIn(Dist.CLIENT)
    public static void effectActivateTotem(BlockPos pos) {
        PlayerEntity player = ClientHelper.getClientPlayer();
        player.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.25F);

        double spread = 1.5D;
        for (int i = 0; i < 8; i++) {
            double px = pos.getX() + 0.5D + (Math.random() - 0.5D) * spread;
            double py = pos.getY() + 0.5D + (Math.random() - 0.5D) * spread;
            double pz = pos.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
            ClientHelper.getClientWorld().addParticle(ParticleTypes.ENCHANT, px, py, pz, 0.0D, 0.1D, 0.0D);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void effectDamageTotem(BlockPos pos) {
        PlayerEntity player = ClientHelper.getClientPlayer();
        player.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.25F);

        double spread = 1.5D;
        for (int i = 0; i < 8; i++) {
            double px = pos.getX() + 0.5D + (Math.random() - 0.5D) * spread;
            double py = pos.getY() + 0.5D + (Math.random() - 0.5D) * spread;
            double pz = pos.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
            ClientHelper.getClientWorld().addParticle(ParticleTypes.SMOKE, px, py, pz, 0.0D, 0.1D, 0.0D);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void effectDestroyTotem(BlockPos pos) {
        PlayerEntity player = ClientHelper.getClientPlayer();
        player.playSound(SoundEvents.ITEM_TOTEM_USE, 0.8F, 1.0F);

        double spread = 1.5D;
        for (int i = 0; i < 4; i++) {
            double px = pos.getX() + 0.5D + (Math.random() - 0.5D) * spread;
            double py = pos.getY() + 0.5D + (Math.random() - 0.5D) * spread;
            double pz = pos.getZ() + 0.5D + (Math.random() - 0.5D) * spread;
            ClientHelper.getClientWorld().addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz, 0.0D, 0.1D, 0.0D);
        }
    }
}
