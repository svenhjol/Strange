package svenhjol.strange.enchantments.module;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.enchantments.iface.ILegendaryItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.ENCHANTMENTS, hasSubscriptions = true)
public class LegendaryTools extends MesonModule implements ILegendaryItem
{
    @Config(name = "Valid enchantments", description = "Valid enchantments that can be applied to Legendary tools.")
    public static List<String> validEnchantments = new ArrayList<>(Arrays.asList(
        "minecraft:efficiency",
        "minecraft:fortune",
        "minecraft:knockback",
        "minecraft:fire_aspect",
        "strange:repel",
        "strange:shulking"
    ));

    @Override
    public boolean shouldRunSetup()
    {
        return Meson.isModuleEnabled("strange:treasure_enchantments");
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event)
    {
        TreasureEnchantments.availableEnchantments.add(this);
    }

    @Override
    public ItemStack getItemStack()
    {
        Random rand = new Random();
        List<ItemStack> items = new ArrayList<>(Arrays.asList(
            new ItemStack(Items.DIAMOND_PICKAXE),
            new ItemStack(Items.DIAMOND_SHOVEL),
            new ItemStack(Items.DIAMOND_AXE),
            new ItemStack(Items.DIAMOND_HOE)
        ));

        return items.get(rand.nextInt(items.size()));
    }

    @Override
    public List<String> getValidEnchantments()
    {
        return validEnchantments;
    }

    @Override
    public int getMaxAdditionalLevels()
    {
        return TreasureEnchantments.legendaryLevels;
    }
}
