package svenhjol.strange.feature.quests;

import net.minecraft.network.chat.Component;
import svenhjol.strange.feature.quests.battle.BattleQuest;
import svenhjol.strange.feature.quests.battle.BattleQuestRenderer;
import svenhjol.strange.feature.quests.client.BaseQuestRenderer;
import svenhjol.strange.feature.quests.gather.GatherQuest;
import svenhjol.strange.feature.quests.gather.GatherQuestRenderer;
import svenhjol.strange.feature.quests.hunt.HuntQuest;
import svenhjol.strange.feature.quests.hunt.HuntQuestRenderer;
import svenhjol.strange.feature.quests.treasure.TreasureQuest;
import svenhjol.strange.feature.quests.treasure.TreasureQuestRenderer;

@SuppressWarnings("unchecked")
public enum QuestType {
    TREASURE(TreasureQuest.class, TreasureQuestRenderer.class, Component.translatable("gui.strange.quests.treasure")),
    BATTLE(BattleQuest.class, BattleQuestRenderer.class, Component.translatable("gui.strange.quests.battle")),
    GATHER(GatherQuest.class, GatherQuestRenderer.class, Component.translatable("gui.strange.quests.gather")),
    HUNT(HuntQuest.class, HuntQuestRenderer.class, Component.translatable("gui.strange.quests.hunt"));

    final Class<? extends Quest> clazz;
    final Class<? extends BaseQuestRenderer<? extends Quest>> renderer;
    final Component typeName; // TODO: deprecated

    QuestType(
        Class<? extends Quest> clazz,
        Class<? extends BaseQuestRenderer<? extends Quest>> renderer,
        Component typeName
    ) {
        this.clazz = clazz;
        this.renderer = renderer;
        this.typeName = typeName;
    }

    public <Q extends Quest> Q makeQuest() {
        Q quest;

        try {
            quest = (Q) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot create quest");
        }

        return quest;
    }

    public <Q extends Quest, R extends BaseQuestRenderer<Q>> R makeRenderer(Q quest) {
        R renderer;

        try {
            renderer = (R) this.renderer.getDeclaredConstructor().newInstance();
            renderer.setQuest(quest);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create renderer");
        }

        return renderer;
    }

    public Component getTypeName() {
        return typeName;
    }
}
