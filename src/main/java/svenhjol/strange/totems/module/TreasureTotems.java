package svenhjol.strange.totems.module;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.LootHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.totems.iface.ITreasureTotem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class TreasureTotems extends MesonModule
{
    @Config(name = "Only in vaults", description = "If true, Strange treasure totems can only be found in Vaults, not normal dungeon loot.")
    public static boolean onlyVaults = true;

    public static List<ITreasureTotem> availableTotems = new ArrayList<>();

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event)
    {
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
    public static ItemStack getTreasureItem(Random rand)
    {
        ITreasureTotem totem = availableTotems.get(rand.nextInt(availableTotems.size()));
        return totem.getTreasureItem();
    }
}
