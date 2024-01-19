package svenhjol.strange.feature.quests;

import net.minecraft.network.chat.Component;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.quests.client.BaseQuestRenderer;
import svenhjol.strange.feature.quests.client.GatherQuestRenderer;
import svenhjol.strange.feature.quests.quest.GatherQuest;

@SuppressWarnings("unchecked")
public enum QuestType {
    GATHER(GatherQuest.class, GatherQuestRenderer.class, TextHelper.translatable("gui.strange.quests.gathering"));

    final Class<? extends Quest<?>> questClass;
    final Class<? extends BaseQuestRenderer<? extends Quest<?>>> questRenderer;
    final Component questTypeName;

    QuestType(
        Class<? extends Quest<?>> questClass,
        Class<? extends BaseQuestRenderer<? extends Quest<?>>> questRenderer,
        Component questTypeName
    ) {
        this.questClass = questClass;
        this.questRenderer = questRenderer;
        this.questTypeName = questTypeName;
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

    public Component getTypeName() {
        return questTypeName;
    }
}
