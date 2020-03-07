package svenhjol.strange.enchantments.module;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.enchantments.iface.ITreasureEnchantment;

import java.util.HashMap;
import java.util.Map;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.ENCHANTMENTS, hasSubscriptions = true)
public class EldritchBow extends MesonModule implements ITreasureEnchantment {
    @Override
    public boolean shouldRunSetup() {
        return Meson.isModuleEnabled("strange:treasure_enchantments");
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event) {
        TreasureEnchantments.availableEnchantments.add(this);
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        HashMap<Enchantment, Integer> map = new HashMap<>();
        map.put(Enchantments.INFINITY, 1);
        map.put(Enchantments.MENDING, 1);
        return map;
    }

    @Override
    public ItemStack getTreasureItem() {
        ItemStack treasure = new ItemStack(Items.BOW);
        treasure.setDisplayName(new TranslationTextComponent("item.strange.treasure.eldritch_bow"));
        return treasure;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.BROWN;
    }
}
