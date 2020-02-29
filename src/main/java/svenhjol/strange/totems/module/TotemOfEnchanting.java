package svenhjol.strange.totems.module;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.loot.TreasureTotem;
import svenhjol.strange.totems.iface.ITreasureTotem;
import svenhjol.strange.totems.item.TotemOfEnchantingItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class TotemOfEnchanting extends MesonModule implements ITreasureTotem
{
    public static TotemOfEnchantingItem item;

    @Override
    public boolean shouldBeEnabled()
    {
        return Meson.isModuleEnabled("strange:treasure_totems");
    }

    @Override
    public void init()
    {
        item = new TotemOfEnchantingItem(this);
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event)
    {
        TreasureTotem.availableTotems.add(this);
    }

    // disabled for now
    public void onAnvilUpdate(AnvilUpdateEvent event)
    {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        ItemStack out;

        int baseCost = Meson.isModuleEnabled("charm:no_anvil_minimum_xp") ? 0 : 1;

        if (left.isEmpty() || right.isEmpty()) return;
        if (right.getItem() != item) return;

        ListNBT leftTags = left.getEnchantmentTagList();
        if (leftTags.isEmpty()) return;

        Map<Enchantment, Integer> inEnchants = EnchantmentHelper.getEnchantments(left);
        List<Enchantment> eligible = new ArrayList<>();

        for (Map.Entry<Enchantment, Integer> entry : inEnchants.entrySet()) {
            Enchantment ench = entry.getKey();
            if (ench == null) continue;
            if (ench.getMaxLevel() <= 1) continue;

            int level = entry.getValue();
            if (level >= ench.getMaxLevel()) continue;

            eligible.add(ench);
        }
        if (eligible.isEmpty()) return;

        Random rand = new Random();
        rand.setSeed(eligible.hashCode());

        Enchantment ench = eligible.get(rand.nextInt(eligible.size()));
        Map<Enchantment, Integer> outEnchants = EnchantmentHelper.getEnchantments(left);
        int level = outEnchants.get(ench);
        outEnchants.put(ench, level + 1);

        out = left.copy();
        EnchantmentHelper.setEnchantments(outEnchants, out);

        // set the display name on the returned item
        String name = event.getName();
        if (!name.isEmpty()) {
            out.setDisplayName(new StringTextComponent(name));
        }

        event.setCost(baseCost);
        event.setMaterialCost(1);
        event.setOutput(out);
    }

    @Override
    public ItemStack getTreasureItem()
    {
        return new ItemStack(item);
    }
}
