package svenhjol.strange.feature.travel_journal;

import net.minecraft.client.Minecraft;
import svenhjol.strange.feature.travel_journal.client.screen.TravelJournalScreen;
import svenhjol.strange.feature.travel_journal.client.screen.HomeScreen;

import java.util.function.Supplier;

public class PageTracker {
    private static Supplier<TravelJournalScreen> screen;
    private static Class<? extends TravelJournalScreen> clazz;

    public static void set(Supplier<TravelJournalScreen> screen) {
        PageTracker.screen = screen;
        PageTracker.clazz = null;
    }

    public static void set(Class<? extends TravelJournalScreen> clazz) {
        PageTracker.clazz = clazz;
        PageTracker.screen = null;
    }

    public static void open() {
        var minecraft = Minecraft.getInstance();

        if (clazz != null) {
            // Create a new instance of the last screen
            try {
                minecraft.setScreen(clazz.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                minecraft.setScreen(new HomeScreen());
            }
        } else if (screen != null) {
            // Restore the previous screen state completely
            minecraft.setScreen(screen.get());
        } else {
            // Default to opening the home screen
            minecraft.setScreen(new HomeScreen());
        }
    }
}
