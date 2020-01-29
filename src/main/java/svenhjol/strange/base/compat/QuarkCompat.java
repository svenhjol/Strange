package svenhjol.strange.base.compat;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import svenhjol.meson.MesonLoader;
import vazkii.quark.tools.module.AncientTomesModule;

import java.util.List;
import java.util.Random;

public class QuarkCompat
{
    public boolean hasModule(ResourceLocation res)
    {
        return MesonLoader.hasModule(res);
    }

    public ItemStack getRandomAncientTome(Random rand)
    {
        List<Enchantment> validEnchants = AncientTomesModule.validEnchants;
        ItemStack tome = new ItemStack(AncientTomesModule.ancient_tome);

        Enchantment enchantment = validEnchants.get(rand.nextInt(validEnchants.size()));
        EnchantedBookItem.addEnchantment(tome, new EnchantmentData(enchantment, enchantment.getMaxLevel()));

        return tome;
    }
}
