package svenhjol.strange.traveljournal.module;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.traveljournal.item.TravelJournalItem;
import svenhjol.strange.traveljournal.proxy.ITravelJournalProxy;
import svenhjol.strange.traveljournal.proxy.TravelJournalClientProxy;
import svenhjol.strange.traveljournal.proxy.TravelJournalServerProxy;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TRAVEL_JOURNAL, hasSubscriptions = true)
public class TravelJournal extends MesonModule
{
    public static TravelJournalItem item;

    public static ITravelJournalProxy proxy;

    @Config(name = "Maximum entries", description = "Maximum number of entries a single travel journal can hold.")
    public static int maxEntries = 15;

    @Override
    public void init()
    {
        item = new TravelJournalItem(this);
    }

    @Override
    public void setup(FMLCommonSetupEvent event)
    {
        proxy = new TravelJournalServerProxy();
    }

    @Override
    public void setupClient(FMLClientSetupEvent event)
    {
        proxy = new TravelJournalClientProxy();
    }
}
