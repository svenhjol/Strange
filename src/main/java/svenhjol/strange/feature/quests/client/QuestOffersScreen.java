package svenhjol.strange.feature.quests.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.trading.Merchant;
import svenhjol.strange.feature.quests.QuestResources;

public class QuestOffersScreen extends Screen {
    protected Merchant villager;
    public QuestOffersScreen(Merchant villager) {
        super(QuestResources.QUEST_OFFERS_TITLE);
        this.villager = villager;
    }
}
