package svenhjol.strange.traveljournal.module;

import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.traveljournal.item.TravelJournalItem;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TRAVEL_JOURNAL)
public class TravelJournal extends MesonModule
{
    public static TravelJournalItem item;

    @Override
    public void init()
    {
        item = new TravelJournalItem(this);
    }
}
