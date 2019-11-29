package svenhjol.strange.scrolls.module;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.LootHelper;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.scrolls.item.ScrollItem;

import java.util.HashMap;
import java.util.Map;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.SCROLLS, hasSubscriptions = true)
public class Scrolls extends MesonModule
{
    public static int MAX_TIERS = 5;

    public static Map<Integer, ScrollItem> tiers = new HashMap<>();

    @Override
    public void init()
    {
        for (int i = 1; i <= MAX_TIERS; i++) {
            tiers.put(i, new ScrollItem(this, i));
        }
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event)
    {
        int weight = 0;
        int quality = 2;

        ResourceLocation res = event.getName();

        if (res.equals(LootTables.CHESTS_WOODLAND_MANSION)) {
            weight = 5;
        } else if (res.equals(LootTables.CHESTS_STRONGHOLD_LIBRARY)) {
            weight = 3;
        } else if (res.equals(LootTables.CHESTS_SIMPLE_DUNGEON)) {
            weight = 1;
        } else if (res.equals(LootTables.CHESTS_PILLAGER_OUTPOST)) {
            weight = 5;
        } else if (res.equals(LootTables.CHESTS_SHIPWRECK_SUPPLY)) {
            weight = 3;
        } else if (res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_DESERT_HOUSE)
            || res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_PLAINS_HOUSE)
            || res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_SAVANNA_HOUSE)
            || res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_SNOWY_HOUSE)
            || res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_TAIGA_HOUSE)
        ) {
            weight = 1;
        }

        if (weight > 0) {
            LootEntry entry = ItemLootEntry.builder(Scrolls.tiers.get(1))
                .weight(weight)
                .quality(quality)
                .acceptFunction(() -> (stack, context) -> {
                    ScrollItem scroll = Scrolls.tiers.get(context.getRandom().nextInt(Scrolls.tiers.size()));
                    return new ItemStack(scroll);
                })
                .build();

            LootHelper.addTableEntry(event.getTable(), entry);
        }
    }
}
