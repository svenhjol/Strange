package svenhjol.strange.feature.quests;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import svenhjol.charmony.api.event.EntityUseEvent;
import svenhjol.charmony.api.event.ScreenSetupEvent;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.charmony.helper.ScreenHelper;
import svenhjol.charmony.iface.ILog;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.quests.Quests.VillagerQuestsResult;
import svenhjol.strange.feature.quests.QuestsNetwork.NotifyVillagerQuestsResult;
import svenhjol.strange.feature.quests.QuestsNetwork.RequestVillagerQuests;
import svenhjol.strange.feature.quests.QuestsNetwork.SyncPlayerQuests;
import svenhjol.strange.feature.quests.QuestsNetwork.SyncVillagerQuests;
import svenhjol.strange.feature.quests.client.QuestOffersScreen;

import java.util.List;
import java.util.UUID;

public class QuestsClient extends ClientFeature {
    static Button questsButton;
    static UUID villagerUuid;
    static VillagerProfession villagerProfession;
    static int villagerLevel;

    @Override
    public Class<? extends CommonFeature> commonFeature() {
        return Quests.class;
    }

    @Override
    public void runWhenEnabled() {
        ScreenSetupEvent.INSTANCE.handle(this::handleScreenSetup);
        EntityUseEvent.INSTANCE.handle(this::handleEntityUse);
    }

    private InteractionResult handleEntityUse(Player player, Level level, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
        villagerUuid = null;
        villagerProfession = null;
        villagerLevel = 1;

        if (entity instanceof Villager villager) {
            villagerUuid = villager.getUUID();
            villagerProfession = villager.getVillagerData().getProfession();
            villagerLevel = villager.getVillagerData().getLevel();
        }

        return InteractionResult.PASS;
    }

    private void handleScreenSetup(Screen screen) {
        var minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        if (!(screen instanceof MerchantScreen merchantScreen)) {
            return;
        }

        if (villagerUuid == null) {
            return;
        }

        var midX = merchantScreen.width / 2;
        var midY = merchantScreen.height / 2;
        var playerUuid = minecraft.player.getUUID();

        // Player at max quests?
        if (Quests.PLAYER_QUESTS.getOrDefault(playerUuid, List.of()).size() >= Quests.maxPlayerQuests) {
            return;
        }

        RequestVillagerQuests.send(villagerUuid);

        questsButton = new QuestsButton(midX - (QuestsButton.WIDTH / 2), midY + 100, b -> {
            screen.onClose();
            openQuestOffers(villagerUuid, villagerProfession, villagerLevel);
        });
        questsButton.visible = false;

        ScreenHelper.addRenderableWidget(merchantScreen, questsButton);
    }

    protected void openQuestOffers(UUID villagerUuid, VillagerProfession villagerProfession, int villagerLevel) {
        Minecraft.getInstance().setScreen(new QuestOffersScreen(villagerUuid, villagerProfession, villagerLevel));
    }

    public static void handleSyncPlayerQuests(SyncPlayerQuests message, Player player) {
        var uuid = player.getUUID();
        var quests = message.getQuests();
        log().debug(QuestsClient.class, "Received " + quests.size() + " player quests");
        Quests.PLAYER_QUESTS.put(uuid, quests);
    }

    public static ILog log() {
        return Mods.client(Strange.ID).log();
    }

    public static void handleSyncVillagerQuests(SyncVillagerQuests message, Player player) {
        var uuid = message.getVillagerUuid();
        var quests = message.getQuests();
        log().debug(QuestsClient.class, "Received " + quests.size() + " quests for villager " + uuid);
        Quests.VILLAGER_QUESTS.put(uuid, quests);
    }

    public static void handleNotifyVillagerQuestsResult(NotifyVillagerQuestsResult message, Player player) {
        var result = message.getResult();
        if (result == VillagerQuestsResult.SUCCESS) {
            questsButton.visible = true;
        }
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
