package svenhjol.strange;

import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony.client.ClientMod;
import svenhjol.strange.feature.ambient_music_discs.AmbientMusicDiscsClient;
import svenhjol.strange.feature.bookmarks.BookmarksClient;
import svenhjol.strange.feature.casks.CasksClient;
import svenhjol.strange.feature.cooking_pots.CookingPotsClient;
import svenhjol.strange.feature.ebony_wood.EbonyWoodClient;
import svenhjol.strange.feature.learned_runes.LearnedRunesClient;
import svenhjol.strange.feature.piglin_pointing.PiglinPointingClient;
import svenhjol.strange.feature.quests.QuestsClient;
import svenhjol.strange.feature.raid_horns.RaidHornsClient;
import svenhjol.strange.feature.runestones.RunestonesClient;
import svenhjol.strange.feature.travel_journal.TravelJournalClient;
import svenhjol.strange.feature.waypoints.WaypointsClient;

import java.util.List;

public class StrangeClient extends ClientMod {

    @Override
    public String modId() {
        return Strange.ID;
    }

    @Override
    public List<Class<? extends ClientFeature>> features() {
        return List.of(
            AmbientMusicDiscsClient.class,
            BookmarksClient.class,
            CasksClient.class,
            CookingPotsClient.class,
            EbonyWoodClient.class,
            LearnedRunesClient.class,
            PiglinPointingClient.class,
            QuestsClient.class,
            RaidHornsClient.class,
            RunestonesClient.class,
            TravelJournalClient.class,
            WaypointsClient.class
        );
    }
}
