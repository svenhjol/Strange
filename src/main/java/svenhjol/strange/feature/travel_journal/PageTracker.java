package svenhjol.strange.feature.travel_journal;

import net.minecraft.client.Minecraft;
import svenhjol.strange.feature.travel_journal.client.screen.TravelJournalScreen;
import svenhjol.strange.feature.travel_journal.client.screen.HomeScreen;

import java.util.function.Supplier;

public class PageTracker {
    private static Supplier<TravelJournalScreen> screen;

    public static void set(Supplier<TravelJournalScreen> screen) {
        PageTracker.screen = screen;
    }
    public static void open() {
        var minecraft = Minecraft.getInstance();

        if (screen != null) {
            // Restore the screen state from the supplier
            minecraft.setScreen(screen.get());
        } else {
            // Default to opening the home screen
            minecraft.setScreen(new HomeScreen());
        }
    }
}
