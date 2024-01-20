package svenhjol.strange.event;

import net.minecraft.world.entity.player.Player;
import svenhjol.charmony.api.event.CharmEvent;
import svenhjol.strange.feature.quests.Quest;

public class QuestEvents {
    public static final AcceptQuestEvent ACCEPT_QUEST = new AcceptQuestEvent();

    public static final AbandonQuestEvent ABANDON_QUEST = new AbandonQuestEvent();

    public static class AcceptQuestEvent extends CharmEvent<AcceptQuestEvent.Handler> {
        private AcceptQuestEvent() {}

        public void invoke(Player player, Quest<?> quest) {
            for (Handler handler : getHandlers()) {
                handler.run(player, quest);
            }
        }

        @FunctionalInterface
        public interface Handler {
            void run(Player player, Quest<?> quest);
        }
    }

    public static class AbandonQuestEvent extends CharmEvent<AbandonQuestEvent.Handler> {
        private AbandonQuestEvent() {}

        public void invoke(Player player, Quest<?> quest) {
            for (Handler handler : getHandlers()) {
                handler.run(player, quest);
            }
        }

        @FunctionalInterface
        public interface Handler {
            void run(Player player, Quest<?> quest);
        }
    }
}
