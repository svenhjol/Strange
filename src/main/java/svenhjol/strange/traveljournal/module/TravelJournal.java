package svenhjol.strange.traveljournal.module;

import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.traveljournal.item.TravelJournalItem;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TRAVEL_JOURNAL, hasSubscriptions = true)
public class TravelJournal extends MesonModule
{

    public static TravelJournalItem item;

    @Config(name = "Maximum entries", description = "Maximum number of entries a single travel journal can hold.")
    public static int maxEntries = 15;

    @Override
    public void init()
    {
        item = new TravelJournalItem(this);
    }

}
