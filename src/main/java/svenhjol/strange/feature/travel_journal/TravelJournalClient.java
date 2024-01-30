package svenhjol.strange.feature.travel_journal;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import org.lwjgl.glfw.GLFW;
import svenhjol.charmony.api.event.KeyPressEvent;
import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.strange.feature.travel_journal.client.screen.HomeScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class TravelJournalClient extends ClientFeature {
    static Supplier<String> openJournalKey;

    public static final List<BiFunction<Integer, Integer, Button>> HOME_BUTTONS = new ArrayList<>();
    public static final List<BiFunction<Integer, Integer, Button>> SHORTCUTS = new ArrayList<>();

    @Override
    public Class<? extends CommonFeature> commonFeature() {
        return TravelJournal.class;
    }

    @Override
    public void register() {
        var registry = mod().registry();

        openJournalKey = registry.key("open_journal",
            () -> new KeyMapping("key.strange.open_journal", GLFW.GLFW_KEY_T, "key.categories.misc"));
    }

    @Override
    public void runWhenEnabled() {
        KeyPressEvent.INSTANCE.handle(this::handleKeyPress);
    }

    public static void registerHomeButton(BiFunction<Integer, Integer, Button> button) {
        HOME_BUTTONS.add(button);
    }

    public static void registerShortcut(BiFunction<Integer, Integer, Button> shortcut) {
        SHORTCUTS.add(shortcut);
    }

    public static void openHomeScreen(Button button) {
        Minecraft.getInstance().setScreen(new HomeScreen());
    }

    private void handleKeyPress(String id) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        if (id.equals(openJournalKey.get())) {
            openJournal();
        }
    }

    private void openJournal() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        PageTracker.open();

        if (minecraft.player != null) {
            minecraft.player.playSound(TravelJournal.interactSound.get(), 0.5f, 1.0f);
        }
    }
}
