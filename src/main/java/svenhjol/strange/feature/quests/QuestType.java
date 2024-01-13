package svenhjol.strange.feature.quests;

import svenhjol.strange.feature.quests.client.BaseQuestRenderer;
import svenhjol.strange.feature.quests.client.GatherQuestRenderer;
import svenhjol.strange.feature.quests.quest.GatherQuest;

@SuppressWarnings("unchecked")
public enum QuestType {
    GATHER(GatherQuest.class, GatherQuestRenderer.class);

    final Class<? extends Quest<?>> questClass;
    final Class<? extends BaseQuestRenderer<? extends Quest<?>>> questRenderer;

    QuestType(Class<? extends Quest<?>> questClass, Class<? extends BaseQuestRenderer<? extends Quest<?>>> questRenderer) {
        this.questClass = questClass;
        this.questRenderer = questRenderer;
    }

    public <Q extends Quest<?>> Q makeQuest() {
        Q quest;

        try {
            quest = (Q) questClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot create quest");
        }

        return quest;
    }

    public <Q extends Quest<?>, R extends BaseQuestRenderer<Q>> R makeRenderer(Q quest) {
        R renderer;

        try {
            renderer = (R) questRenderer.getDeclaredConstructor().newInstance();
            renderer.setQuest(quest);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create renderer");
        }

        return renderer;
    }
}
