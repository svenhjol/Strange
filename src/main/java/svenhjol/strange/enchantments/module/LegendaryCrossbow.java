package svenhjol.strange.enchantments.module;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.enchantments.iface.ILegendaryItem;

import java.util.*;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.ENCHANTMENTS, hasSubscriptions = true)
public class LegendaryCrossbow extends MesonModule implements ILegendaryItem {
    @Config(name = "Valid enchantments", description = "Valid enchantments that can be applied to Legendary crossbows.")
    public static List<String> validEnchantments = new ArrayList<>(Arrays.asList(
        "minecraft:quick_charge",
        "minecraft:piercing"
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
    public ItemStack getItemStack(){
        Random rand = new Random();
        List<ItemStack> items = new ArrayList<>();
        Tag<Item> legendaryItemsTag = ItemTags.getCollection().get(new ResourceLocation("strange:legendary_crossbows"));

        //If the tag is missing for some reasons use the default entry
        if (legendaryItemsTag == null){
            return new ItemStack(Items.CROSSBOW);
        }
        else {
            Collection<Item> tagItems = legendaryItemsTag.getAllElements();
            for (Item item : tagItems) {
                items.add(new ItemStack(item));
            }
        }

        return items.get(rand.nextInt(items.size()));
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
