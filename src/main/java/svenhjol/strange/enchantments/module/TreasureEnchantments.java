package svenhjol.strange.enchantments.module;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.charm.Charm;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.LootHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.enchantments.iface.ITreasureEnchantment;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.ENCHANTMENTS, hasSubscriptions = true,
    description = "Treasure items and Legendary items are specially enchanted weapons and tools found in Vaults.")
public class TreasureEnchantments extends MesonModule {
    @Config(name = "Obtainable enchantments", description = "If true, Strange treasure enchantments can be applied to\n" +
        "books or tools on an enchanting table or anvil.")
    public static boolean obtainable = false;

    @Config(name = "Only in vaults", description = "If true, Strange treasure enchantments can only be found in Vaults, not normal dungeon loot.")
    public static boolean onlyVaults = true;

    @Config(name = "Legendary levels", description = "The potential number of additional levels above the enchantment's maximum level.\n" +
        "An item can never be enchanted higher than level 10.")
    public static int legendaryLevels = 3;

    public static final List<ITreasureEnchantment> availableEnchantments = new ArrayList<>();

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        if (onlyVaults) return;

        int weight = 0;
        int quality = 1;

        ResourceLocation res = event.getName();

        if (res.equals(LootTables.CHESTS_NETHER_BRIDGE)) {
            weight = 10;
        } else if (res.equals(LootTables.CHESTS_END_CITY_TREASURE)) {
            weight = 10;
        } else if (res.equals(LootTables.CHESTS_SIMPLE_DUNGEON)) {
            weight = 1;
        }

        if (weight > 0) {
            LootEntry entry = ItemLootEntry.builder(Items.DIAMOND)
                .weight(weight)
                .quality(quality)
                .acceptFunction(() -> (stack, context) -> {
                    ItemStack treasure = getTreasureItem(context.getRandom());
                    if (treasure != null)
                        return treasure;

                    return stack;
                })
                .build();

            LootTable table = event.getTable();
            LootHelper.addTableEntry(table, entry);
        }
    }

    @Nullable
    public static ItemStack getTreasureItem(Random rand) {
        ITreasureEnchantment ench = TreasureEnchantments.availableEnchantments.get(rand.nextInt(TreasureEnchantments.availableEnchantments.size()));

        ItemStack treasure = ench.getTreasureItem();
        DyeColor color = ench.getColor();

        Map<Enchantment, Integer> map = ench.getEnchantments();
        if (map == null || map.isEmpty()) return null;

        EnchantmentHelper.setEnchantments(map, treasure);

        if (Charm.quarkCompat != null && Charm.quarkCompat.hasColorRuneModule())
            Charm.quarkCompat.applyColor(treasure, color);

        return treasure;
    }
}
