package svenhjol.strange.base.compat;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import svenhjol.meson.MesonLoader;
import vazkii.quark.tools.module.AncientTomesModule;
import vazkii.quark.world.block.CaveCrystalBlock;
import vazkii.quark.world.module.BigDungeonModule;

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

    public boolean isInsideBigDungeon(ServerWorld world, BlockPos pos)
    {
        return BigDungeonModule.structure.isPositionInsideStructure(world, pos);
    }

    public boolean isCrystal(World world, BlockPos pos)
    {
        BlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof CaveCrystalBlock;
    }
}
