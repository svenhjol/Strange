package svenhjol.strange.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import svenhjol.strange.module.quests.Quest;

public final class QuestEvents {
    public static final Event<Invoker> START = EventFactory.createArrayBacked(Invoker.class, callbacks -> quest -> {
        for (Invoker callback : callbacks) {
            callback.invoke(quest);
        }
    });

    public static final Event<Invoker> PAUSE = EventFactory.createArrayBacked(Invoker.class, callbacks -> quest -> {
        for (Invoker callback : callbacks) {
            callback.invoke(quest);
        }
    });

    public static final Event<Invoker> ABANDON = EventFactory.createArrayBacked(Invoker.class, callbacks -> quest -> {
        for (Invoker callback : callbacks) {
            callback.invoke(quest);
        }
    });

    public static final Event<Invoker> COMPLETE = EventFactory.createArrayBacked(Invoker.class, callbacks -> quest -> {
        for (Invoker callback : callbacks) {
            callback.invoke(quest);
        }
    });

    @FunctionalInterface
    public interface Invoker {
        void invoke(Quest quest);
    }
}

