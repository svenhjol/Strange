package svenhjol.strange.feature.quests;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.Merchant;
import svenhjol.charmony.api.event.ScreenSetupEvent;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.charmony.helper.ScreenHelper;
import svenhjol.charmony.iface.ILog;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.quests.QuestsNetwork.SyncQuests;
import svenhjol.strange.feature.quests.client.QuestOffersScreen;

import java.util.List;

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

        // Handle the villager trading screen button
        if (!(screen instanceof MerchantScreen merchantScreen)) {
            return;
        }

        var midX = merchantScreen.width / 2;
        var midY = merchantScreen.height / 2;

        var menu = merchantScreen.getMenu();
        var villager = menu.trader;
        var uuid = minecraft.player.getUUID();

        // Player at max quests?
        if (Quests.PLAYER_QUESTS.getOrDefault(uuid, List.of()).size() >= Quests.maxQuests) {
            return;
        }

        questsButton = new QuestsButton(midX - (QuestsButton.WIDTH / 2), midY + 100,
            b -> openQuestOffers(villager));

        ScreenHelper.addRenderableWidget(merchantScreen, questsButton);
    }

    protected void openQuestOffers(Merchant villager) {
        Minecraft.getInstance().setScreen(new QuestOffersScreen(villager));
    }

    public static void handleSyncQuests(SyncQuests message, Player player) {
        var uuid = player.getUUID();
        var quests = message.getQuests();
        log().debug(QuestsClient.class, "Received " + quests.size() + " quests");
        Quests.PLAYER_QUESTS.put(uuid, quests);
    }

    public static ILog log() {
        return Mods.client(Strange.ID).log();
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
