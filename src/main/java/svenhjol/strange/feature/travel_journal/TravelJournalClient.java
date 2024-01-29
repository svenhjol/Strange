package svenhjol.strange.feature.travel_journal;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import svenhjol.charmony.api.event.KeyPressEvent;
import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.strange.event.QuestEvents;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.client.QuestOffersScreen;
import svenhjol.strange.feature.quests.client.QuestScreen;
import svenhjol.strange.feature.quests.client.QuestsScreen;

import java.util.function.Supplier;

public class TravelJournalClient extends ClientFeature {
    static Supplier<String> openJournalKey;

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
        QuestEvents.ACCEPT_QUEST.handle(this::handleAcceptQuest);
        QuestEvents.ABANDON_QUEST.handle(this::handleAbandonQuest);
    }

    private void handleAbandonQuest(Player player, Quest quest) {
        if (!player.level().isClientSide) return;
        var minecraft = Minecraft.getInstance();

        if (minecraft.screen instanceof QuestScreen) {
            PageTracker.quest = null;
            minecraft.setScreen(new QuestsScreen());
        }
    }

    private void handleAcceptQuest(Player player, Quest quest) {
        if (!player.level().isClientSide) return;
        var minecraft = Minecraft.getInstance();

        if (minecraft.screen instanceof QuestOffersScreen) {
            // TODO: toast
            minecraft.setScreen(null);
        }
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

        if (PageTracker.screen == null) {
            PageTracker.Screen.HOME.open();
        } else {
            PageTracker.screen.open();
        }

        if (minecraft.player != null) {
            minecraft.player.playSound(TravelJournal.interactSound.get(), 0.5f, 1.0f);
        }
    }
}
