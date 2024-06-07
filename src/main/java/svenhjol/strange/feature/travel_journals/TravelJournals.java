package svenhjol.strange.feature.travel_journals;

import svenhjol.charm.charmony.annotation.Feature;
import svenhjol.charm.charmony.common.CommonFeature;
import svenhjol.charm.charmony.common.CommonLoader;
import svenhjol.strange.feature.travel_journals.common.Handlers;
import svenhjol.strange.feature.travel_journals.common.Networking;
import svenhjol.strange.feature.travel_journals.common.Registers;

@Feature
public final class TravelJournals extends CommonFeature {
    public static final String PHOTOS_DIR = "strange_travel_journal_photos";
    
    public final Registers registers;
    public final Networking networking;
    public final Handlers handlers;

    public TravelJournals(CommonLoader loader) {
        super(loader);

        registers = new Registers(this);
        networking = new Networking(this);
        handlers = new Handlers(this);
    }
}
