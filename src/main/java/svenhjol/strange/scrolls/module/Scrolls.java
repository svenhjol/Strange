package svenhjol.strange.scrolls.module;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.charm.Charm;
import svenhjol.charm.decoration.module.BookshelfChests;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.LootHelper;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.scrolls.item.ScrollItem;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.SCROLLS, hasSubscriptions = true)
public class Scrolls extends MesonModule
{
    public static int MAX_TIERS = 5;

    public static ScrollItem item;

    @Override
    public void init()
    {
        item = new ScrollItem(this);
    }

    @Override
    public void setup(FMLCommonSetupEvent event)
    {
        if (Charm.loader.hasModule(BookshelfChests.class)) {
            BookshelfChests.validItems.add(ScrollItem.class);
        }
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event)
    {
        int weight = 0;
        int quality = 2;

        ResourceLocation res = event.getName();

        if (res.equals(LootTables.CHESTS_WOODLAND_MANSION)) {
            weight = 6;
        } else if (res.equals(LootTables.CHESTS_STRONGHOLD_LIBRARY)) {
            weight = 3;
        } else if (res.equals(LootTables.CHESTS_SIMPLE_DUNGEON)) {
            weight = 1;
        } else if (res.equals(LootTables.CHESTS_PILLAGER_OUTPOST)) {
            weight = 6;
        } else if (res.equals(LootTables.CHESTS_SHIPWRECK_SUPPLY)) {
            weight = 3;
        } else if (res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_DESERT_HOUSE)
            || res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_PLAINS_HOUSE)
            || res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_SAVANNA_HOUSE)
            || res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_SNOWY_HOUSE)
            || res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_TAIGA_HOUSE)
        ) {
            weight = 2;
        }

        if (weight > 0) {
            LootEntry entry = ItemLootEntry.builder(Scrolls.item)
                .weight(weight)
                .quality(quality)
                .acceptFunction(() -> (scroll, context) -> {
                    int tier = context.getRandom().nextInt(Scrolls.MAX_TIERS) + 1;
                    return ScrollItem.putTier(scroll, tier);
                })
                .build();

            LootHelper.addTableEntry(event.getTable(), entry);
        }
    }
}
