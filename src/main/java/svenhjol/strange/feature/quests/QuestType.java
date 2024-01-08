package svenhjol.strange.feature.quests;

import svenhjol.strange.feature.quests.quest.GatherQuest;

public enum QuestType {
    GATHER(GatherQuest.class);

    final Class<? extends Quest<?>> clazz;

    QuestType(Class<? extends Quest<?>> clazz) {
        this.clazz = clazz;
    }

    @SuppressWarnings("unchecked")
    public <Q extends Quest<?>> Q instance() {
        Q quest;

        try {
            quest = (Q)clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot create quest");
        }

        return quest;
    }
}
