package svenhjol.strange.feature.travel_journal;

import net.minecraft.world.entity.player.Player;
import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.strange.feature.travel_journal.TravelJournalNetwork.SentTravelJournalLearned;

public class TravelJournalClient extends ClientFeature {
    @Override
    public Class<? extends CommonFeature> commonFeature() {
        return TravelJournal.class;
    }

    public static void handleSentLearned(SentTravelJournalLearned message, Player player) {
        TravelJournal.LEARNED.put(player.getUUID(), message.getLearned());
    }
}
