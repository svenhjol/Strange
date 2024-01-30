package svenhjol.strange.feature.learned_runes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Player;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.learned_runes.LearnedRunesNetwork.SyncLearned;
import svenhjol.strange.feature.learned_runes.client.LearnedRunesButtons.*;
import svenhjol.strange.feature.learned_runes.client.screen.LearnedScreen;
import svenhjol.strange.feature.travel_journal.TravelJournalClient;

public class LearnedRunesClient extends ClientFeature {
    @Override
    public Class<? extends CommonFeature> commonFeature() {
        return LearnedRunes.class;
    }

    @Override
    public void runWhenEnabled() {
        TravelJournalClient.registerShortcut(
            (x, y) -> new LearnedShortcutButton(x, y, LearnedRunesClient::openLearnedScreen));

        TravelJournalClient.registerHomeButton(
            (x, y) -> new LearnedButton(x - (LearnedButton.WIDTH / 2), y, LearnedRunesClient::openLearnedScreen));
    }

    public static void handleSyncLearned(SyncLearned message, Player player) {
        logDebugMessage("Received learned from server with " + message.getLearned().all().size() + " location(s)");
        LearnedRunes.LEARNED.put(player.getUUID(), message.getLearned());
    }

    public static void openLearnedScreen(Button button) {
        Minecraft.getInstance().setScreen(new LearnedScreen());
    }

    public static void openLearnedScreen(int page) {
        Minecraft.getInstance().setScreen(new LearnedScreen(page));
    }

    private static void logDebugMessage(String message) {
        Mods.client(Strange.ID).log().debug(TravelJournalClient.class, message);
    }
}
