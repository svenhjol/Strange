package svenhjol.strange.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.strange.module.quests.Quest;

public final class QuestEvents {
    public static final Event<QuestPlayerInvoker> START = EventFactory.createArrayBacked(QuestPlayerInvoker.class, callbacks -> (quest, player) -> {
        for (QuestPlayerInvoker callback : callbacks) {
            callback.invoke(quest, player);
        }
    });

    public static final Event<QuestPlayerInvoker> PAUSE = EventFactory.createArrayBacked(QuestPlayerInvoker.class, callbacks -> (quest, player) -> {
        for (QuestPlayerInvoker callback : callbacks) {
            callback.invoke(quest, player);
        }
    });

    public static final Event<QuestPlayerInvoker> ABANDON = EventFactory.createArrayBacked(QuestPlayerInvoker.class, callbacks -> (quest, player) -> {
        for (QuestPlayerInvoker callback : callbacks) {
            callback.invoke(quest, player);
        }
    });

    public static final Event<QuestPlayerInvoker> COMPLETE = EventFactory.createArrayBacked(QuestPlayerInvoker.class, callbacks -> (quest, player) -> {
        for (QuestPlayerInvoker callback : callbacks) {
            callback.invoke(quest, player);
        }
    });

    public static final Event<QuestPlayerInvoker> UPDATE = EventFactory.createArrayBacked(QuestPlayerInvoker.class, callbacks -> (quest, player) -> {
        for (QuestPlayerInvoker callback : callbacks) {
            callback.invoke(quest, player);
        }
    });

    public static final Event<QuestPlayerInvoker> REMOVE = EventFactory.createArrayBacked(QuestPlayerInvoker.class, callbacks -> (quest, player) -> {
        for (QuestPlayerInvoker callback : callbacks) {
            callback.invoke(quest, player);
        }
    });

    @FunctionalInterface
    public interface QuestPlayerInvoker {
        void invoke(Quest quest, ServerPlayer player);
    }
}

