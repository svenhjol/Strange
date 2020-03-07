package svenhjol.strange.scrolls.module;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.charm.decoration.module.BookshelfChests;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.LootHelper;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.scrolls.item.ScrollItem;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.SCROLLS, hasSubscriptions = true,
    description = "Scrolls contain information about quests.\n" +
        "They can be found in chests or bought from scrollkeeper villagers.")
public class Scrolls extends MesonModule {
    public static int MAX_TIERS = 5;
    public static ScrollItem item;

    @Override
    public void init() {
        item = new ScrollItem(this);
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event) {
        if (Meson.isModuleEnabled("charm:bookshelf_chests"))
            BookshelfChests.validItems.add(ScrollItem.class);
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
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
            weight = 2;
        } else if (res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_DESERT_HOUSE)
            || res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_PLAINS_HOUSE)
            || res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_SAVANNA_HOUSE)
            || res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_SNOWY_HOUSE)
            || res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_TAIGA_HOUSE)
        ) {
            weight = 1;
        }

        if (weight > 0) {
            LootEntry entry = ItemLootEntry.builder(Scrolls.item)
                .weight(weight)
                .quality(quality)
                .acceptFunction(() -> (scroll, context) -> createScroll(scroll,
                    context.getRandom().nextInt(Scrolls.MAX_TIERS - 1) + 1,
                    0.25F * context.getRandom().nextInt(4) + 2))
                .build();

            LootHelper.addTableEntry(event.getTable(), entry);
        }
    }

    public static ItemStack createScroll(ItemStack scroll, int tier, float value) {
        ScrollItem.putTier(scroll, tier);
        ScrollItem.putValue(scroll, value);
        return scroll;
    }

    public static ItemStack createScroll(int tier, float value) {
        ItemStack scroll = new ItemStack(item);
        return createScroll(scroll, tier, value);
    }
}
