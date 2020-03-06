package svenhjol.strange.scrolls.module;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import svenhjol.charm.decoration.module.BookshelfChests;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.LootHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.scrolls.Quests;
import svenhjol.strange.scrolls.Scrollkeepers;
import svenhjol.strange.scrolls.item.ScrollItem;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.SCROLLS, hasSubscriptions = true)
public class Scrolls extends MesonModule
{
    public static final ResourceLocation QUESTS_CAP_ID = new ResourceLocation(Strange.MOD_ID, "quest_capability");
    public static int MAX_TIERS = 5;
    public static ScrollItem item;

    @Config(name = "Maximum quests", description = "Maximum number of quests a player can do at once (Maximum 3)")
    public static int maxQuests = 2; // don't get this value directly, use getMaxQuests()

    @Config(name = "Encounter distance", description = "Distance from quest start (in blocks) that a mob will spawn for 'encounter' quests.")
    public static int encounterDistance = 600;

    @Config(name = "Locate distance", description = "Distance from quest start (in blocks) that a treasure chest will spawn for 'locate' quests.")
    public static int locateDistance = 400;

    @Config(name = "Language", description = "Language code to use for showing quest details.")
    public static String language = "en";

    @Config(name = "Bad Omen chance", description = "Chance (out of 1.0) of a Bad Omen effect being applied after quest completion.\n" +
        "The chance and severity of the Bad Omen effect increases with Scrollkeeper level and distance from spawn.  Set to zero to disable Bad Omen effect.")
    public static double badOmenChance = 0.025D;

    @Config(name = "Villager interest range", description = "Range (in blocks) that a scrollkeeper will indicate that they are ready to accept a completed quest.")
    public static int interestRange = 16;

    public Quests quests;
    public Scrollkeepers scrollkeepers;

    @Override
    public void init()
    {
        item = new ScrollItem(this);
        quests = new Quests(this);
        scrollkeepers = new Scrollkeepers(this);

        scrollkeepers.init();
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event)
    {
        quests.onCommonSetup(event);

        MinecraftForge.EVENT_BUS.register(scrollkeepers);

        if (Meson.isModuleEnabled("charm:bookshelf_chests"))
            BookshelfChests.validItems.add(ScrollItem.class);
    }

    @Override
    public void onClientSetup(FMLClientSetupEvent event)
    {
        quests.onClientSetup(event);
    }

    @Override
    public void onServerStarted(FMLServerStartedEvent event)
    {
        quests.onServerStarted(event);
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event)
    {
        int weight = 0;
        int quality = 2;
        ResourceLocation res = event.getName();

        if (res.equals(LootTables.CHESTS_WOODLAND_MANSION)) { weight = 6; }
        else if (res.equals(LootTables.CHESTS_STRONGHOLD_LIBRARY)) { weight = 3; }
        else if (res.equals(LootTables.CHESTS_SIMPLE_DUNGEON)) { weight = 1; }
        else if (res.equals(LootTables.CHESTS_PILLAGER_OUTPOST)) { weight = 6; }
        else if (res.equals(LootTables.CHESTS_SHIPWRECK_SUPPLY)) { weight = 3; }
        else if (res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_DESERT_HOUSE)
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
                .acceptFunction(() -> (scroll, context) -> createScroll(scroll,
                    context.getRandom().nextInt(Scrolls.MAX_TIERS - 1) + 1,
                    0.25F * context.getRandom().nextInt(4) + 2))
                .build();

            LootHelper.addTableEntry(event.getTable(), entry);
        }
    }

    public static ItemStack createScroll(ItemStack scroll, int tier, float value)
    {
        ScrollItem.putTier(scroll, tier);
        ScrollItem.putValue(scroll, value);
        return scroll;
    }

    public static ItemStack createScroll(int tier, float value)
    {
        ItemStack scroll = new ItemStack(item);
        return createScroll(scroll, tier, value);
    }
}
