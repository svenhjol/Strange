package svenhjol.strange.feature.travel_journals.common;

import svenhjol.charm.charmony.common.item.CharmItem;
import svenhjol.strange.feature.travel_journals.TravelJournals;

public class TravelJournalItem extends CharmItem<TravelJournals> {
    public TravelJournalItem() {
        super(new Properties());
    }

    @Override
    public Class<TravelJournals> typeForFeature() {
        return TravelJournals.class;
    }
}
