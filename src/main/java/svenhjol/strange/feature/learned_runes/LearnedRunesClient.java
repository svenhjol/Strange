package svenhjol.strange.feature.learned_runes;

import net.minecraft.world.entity.player.Player;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.learned_runes.LearnedRunesNetwork.SyncLearned;
import svenhjol.strange.feature.travel_journal.TravelJournalClient;

public class LearnedRunesClient extends ClientFeature {
    @Override
    public Class<? extends CommonFeature> commonFeature() {
        return LearnedRunes.class;
    }

    public static void handleSyncLearned(SyncLearned message, Player player) {
        logDebugMessage("Received learned from server with " + message.getLearned().getLocations().size() + " location(s)");
        LearnedRunes.LEARNED.put(player.getUUID(), message.getLearned());
    }

    private static void logDebugMessage(String message) {
        Mods.client(Strange.ID).log().debug(TravelJournalClient.class, message);
    }
}
