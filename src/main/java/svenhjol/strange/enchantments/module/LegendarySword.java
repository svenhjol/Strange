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

@Module(mod = Strange.MOD_ID, category = StrangeCategories.ENCHANTMENTS, hasSubscriptions = true)
public class LegendarySword extends MesonModule implements ILegendaryItem {
    @Config(name = "Valid enchantments", description = "Valid enchantments that can be applied to Legendary swords.")
    public static List<String> validEnchantments = new ArrayList<>(Arrays.asList(
        "minecraft:sharpness",
        "minecraft:smite",
        "minecraft:bane_of_arthropods",
        "minecraft:knockback",
        "minecraft:fire_aspect",
        "minecraft:looting",
        "minecraft:sweeping",
        "strange:ice_aspect",
        "strange:repel",
        "strange:shulking"
    ));

    @Override
    public boolean shouldRunSetup() {
        return Meson.isModuleEnabled("strange:treasure_enchantments");
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event) {
        TreasureEnchantments.availableEnchantments.add(this);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(Items.DIAMOND_SWORD);
    }

    @Override
    public List<String> getValidEnchantments() {
        return validEnchantments;
    }

    @Override
    public int getMaxAdditionalLevels() {
        return TreasureEnchantments.legendaryLevels;
    }
}
