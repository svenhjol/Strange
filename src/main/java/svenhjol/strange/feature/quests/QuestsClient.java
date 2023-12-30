package svenhjol.strange.feature.quests;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.trading.Merchant;
import svenhjol.charmony.api.event.ScreenSetupEvent;
import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.charmony.helper.ScreenHelper;
import svenhjol.strange.feature.quests.client.QuestOffersScreen;

public class QuestsClient extends ClientFeature {
    static Button questsButton;

    @Override
    public Class<? extends CommonFeature> commonFeature() {
        return Quests.class;
    }

    @Override
    public void register() {
        ScreenSetupEvent.INSTANCE.handle(this::handleScreenSetup);
    }

    private void handleScreenSetup(Screen screen) {
        var minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        if (!(screen instanceof MerchantScreen merchantScreen)) {
            return;
        }

        var midX = merchantScreen.width / 2;
        var midY = merchantScreen.height / 2;

        var menu = merchantScreen.getMenu();
        var villager = menu.trader;

        questsButton = new QuestsButton(midX - (QuestsButton.WIDTH / 2), midY + 100, b -> {
            mod().log().debug(getClass(), "here");
        });

        ScreenHelper.addRenderableWidget(merchantScreen, questsButton);
    }

    protected void openQuestOffers(Merchant villager) {
        Minecraft.getInstance().setScreen(new QuestOffersScreen(villager));
    }

    static class QuestsButton extends Button {
        static int WIDTH = 110;
        static int HEIGHT = 20;
        static Component TEXT = QuestResources.QUEST_BUTTON_TEXT;
        public QuestsButton(int x, int y, Button.OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }
}
