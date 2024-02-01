package svenhjol.strange.feature.quests;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.EntityHitResult;
import svenhjol.charmony.api.event.ClientStartEvent;
import svenhjol.charmony.api.event.EntityUseEvent;
import svenhjol.charmony.api.event.PlayerTickEvent;
import svenhjol.charmony.api.event.ScreenSetupEvent;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.charmony.helper.ScreenHelper;
import svenhjol.charmony.iface.ILog;
import svenhjol.strange.Strange;
import svenhjol.strange.event.QuestEvents;
import svenhjol.strange.feature.quests.Quests.VillagerQuestsResult;
import svenhjol.strange.feature.quests.QuestsNetwork.*;
import svenhjol.strange.feature.quests.client.QuestsButtons.QuestsButton;
import svenhjol.strange.feature.quests.client.QuestsButtons.QuestsShortcutButton;
import svenhjol.strange.feature.quests.client.QuestsButtons.VillagerQuestsButton;
import svenhjol.strange.feature.quests.client.screen.QuestOffersScreen;
import svenhjol.strange.feature.quests.client.screen.QuestScreen;
import svenhjol.strange.feature.quests.client.screen.QuestsScreen;
import svenhjol.strange.feature.travel_journal.TravelJournalClient;

import java.util.*;

public class QuestsClient extends ClientFeature {
    private static final Map<UUID, QuestList> PLAYER_QUESTS = new HashMap<>();
    private static final Map<UUID, QuestList> VILLAGER_QUESTS = new HashMap<>();
    static Button villagerQuestsButton;
    static UUID villagerUuid;
    static VillagerProfession villagerProfession;
    static int villagerLevel;

    @Override
    public Class<? extends CommonFeature> commonFeature() {
        return Quests.class;
    }

    @Override
    public void runWhenEnabled() {
        ClientStartEvent.INSTANCE.handle(this::handleClientStart);
        ScreenSetupEvent.INSTANCE.handle(this::handleScreenSetup);
        EntityUseEvent.INSTANCE.handle(this::handleEntityUse);
        PlayerTickEvent.INSTANCE.handle(this::handlePlayerTick);
        QuestEvents.ACCEPT_QUEST.handle(this::handleAcceptQuest);
        QuestEvents.ABANDON_QUEST.handle(this::handleAbandonQuest);

        TravelJournalClient.registerShortcut(
            (x, y) -> new QuestsShortcutButton(x, y, QuestsClient::openQuestsScreen));

        TravelJournalClient.registerHomeButton(
            (x, y) -> new QuestsButton(x - (QuestsButton.WIDTH / 2), y, QuestsClient::openQuestsScreen));
    }

    public static void setPlayerQuests(Player player, QuestList quests) {
        PLAYER_QUESTS.put(player.getUUID(), quests);
    }

    public static void setVillagerQuests(UUID villagerUuid, QuestList quests) {
        VILLAGER_QUESTS.put(villagerUuid, quests);
    }

    public static QuestList getPlayerQuests(Player player) {
        return PLAYER_QUESTS.getOrDefault(player.getUUID(), new QuestList());
    }

    public static QuestList getVillagerQuests(UUID villagerUuid) {
        return VILLAGER_QUESTS.getOrDefault(villagerUuid, new QuestList());
    }

    public static Optional<Quest> getPlayerQuest(Player player, String questId) {
        return getPlayerQuests(player).get(questId);
    }

    private void handleAbandonQuest(Player player, Quest quest) {
        if (!player.level().isClientSide) return;
        var minecraft = Minecraft.getInstance();

        if (minecraft.screen instanceof QuestScreen) {
            minecraft.setScreen(new QuestsScreen());
        }
    }

    private void handleAcceptQuest(Player player, Quest quest) {
        if (!player.level().isClientSide) return;
        var minecraft = Minecraft.getInstance();

        if (minecraft.screen instanceof QuestOffersScreen) {
            // TODO: toast
            minecraft.setScreen(null);
        }
    }

    private void handleClientStart(Minecraft minecraft) {
        // Load all the mob sprites.
        QuestsResources.MOB_SPRITES.clear();
        BuiltInRegistries.ENTITY_TYPE.forEach(e -> {
            var id = BuiltInRegistries.ENTITY_TYPE.getKey(e);
            QuestsResources.MOB_SPRITES.put(e, new ResourceLocation(id.getNamespace(), "mobs/" + id.getPath()));
        });

        // Load all the loot sprites.
        QuestsResources.LOOT_SPRITES.clear();
        BuiltInLootTables.all().forEach(
            id -> QuestsResources.LOOT_SPRITES.put(id, new ResourceLocation(id.getNamespace(), "loot/" + id.getPath())));
    }

    private void handlePlayerTick(Player player) {
        if (!player.level().isClientSide) {
            return;
        }

        // Tick all quests on the client
        for (var quest : getPlayerQuests(player).all()) {
            quest.tick(player);
        }

        // Check for nearby villager receivers
        if (player.level().getGameTime() % 15 == 0) {
            showInterestedVillagers(player);
        }
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

        RequestVillagerQuests.send(villagerUuid);

        villagerQuestsButton = new VillagerQuestsButton(midX - (VillagerQuestsButton.WIDTH / 2), midY + 92, b -> {
            screen.onClose();
            openQuestOffers(villagerUuid, villagerProfession, villagerLevel);
        });
        villagerQuestsButton.active = false;

        ScreenHelper.addRenderableWidget(merchantScreen, villagerQuestsButton);
    }

    protected void openQuestOffers(UUID villagerUuid, VillagerProfession villagerProfession, int villagerLevel) {
        Minecraft.getInstance().setScreen(new QuestOffersScreen(villagerUuid, villagerProfession, villagerLevel));
    }

    public static void handleSyncPlayerQuests(SyncPlayerQuests message, Player player) {
        var quests = message.getQuests();
        log().debug(QuestsClient.class, "Received " + quests.size() + " player quests");
        setPlayerQuests(player, quests);
    }

    public static void handleSyncVillagerQuests(SyncVillagerQuests message, Player player) {
        var uuid = message.getVillagerUuid();
        var quests = message.getQuests();
        log().debug(QuestsClient.class, "Received " + quests.size() + " quests for villager " + uuid);
        setVillagerQuests(villagerUuid, quests);
    }

    public static void handleNotifyVillagerQuestsResult(NotifyVillagerQuestsResult message, Player player) {
        var result = message.getResult();
        if (result == VillagerQuestsResult.SUCCESS) {
            villagerQuestsButton.active = true;
        }
    }

    public static void handleAcceptQuestResult(NotifyAcceptQuestResult message, Player player) {
        if (message.getResult().equals(Quests.AcceptQuestResult.SUCCESS)) {
            // Fire AcceptQuestEvent on the client side.
            getPlayerQuest(player, message.getQuestId()).ifPresent(
                quest -> QuestEvents.ACCEPT_QUEST.invoke(player, quest));
        }
    }

    public static void handleAbandonQuestResult(NotifyAbandonQuestResult message, Player player) {
        if (message.getResult().equals(Quests.AbandonQuestResult.SUCCESS)) {
            // Fire AbandonQuestEvent on the client side.
            getPlayerQuest(player, message.getQuestId()).ifPresent(
                quest -> QuestEvents.ABANDON_QUEST.invoke(player, quest));
        }
    }

    public static void openQuestsScreen(Button button) {
        Minecraft.getInstance().setScreen(new QuestsScreen());
    }

    public static void openQuestsScreen(int page) {
        Minecraft.getInstance().setScreen(new QuestsScreen(page));
    }

    public static ILog log() {
        return Mods.client(Strange.ID).log();
    }

    private void showInterestedVillagers(Player player) {
        var level = player.level();
        var pos = player.blockPosition();

        var quests = getPlayerQuests(player);
        List<Villager> receivers = new ArrayList<>();
        List<Villager> questGivers = new ArrayList<>();

        quests.all().forEach(quest -> {
            if (quest.satisfied()) {
                var opt = QuestsHelper.getNearbyMatchingVillager(level, pos, quest.villagerUuid());
                if (opt.isPresent() && !questGivers.contains(opt.get())) {
                    questGivers.add(opt.get());
                }

                var matching = QuestsHelper.getNearbyMatchingProfessions(level, pos, quest.villagerProfessions());
                for (var matched : matching) {
                    if (!matched.getUUID().equals(quest.villagerUuid()) && !receivers.contains(matched)) {
                        receivers.add(matched);
                    }
                }
            }
        });

        receivers.forEach(villager -> {
            var spread = 0.75d;
            var villagerPos = villager.position();
            for (int i = 0; i < 3; i++) {
                var px = villagerPos.x() + (Math.random() - 0.5d) * spread;
                var py = villagerPos.y() + 2.25d + (Math.random() - 0.5d) * spread;
                var pz = villagerPos.z() + (Math.random() - 0.5d) * spread;
                level.addParticle(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0, 0, 0.12d);
            }
        });

        questGivers.forEach(villager -> {
            var spread = 0.55d;
            var villagerPos = villager.position();
            for (int i = 0; i < 2; i++) {
                var px = villagerPos.x() + (Math.random() - 0.5d) * spread;
                var py = villagerPos.y() + 2.6d + (Math.random() - 0.5d) * spread;
                var pz = villagerPos.z() + (Math.random() - 0.5d) * spread;
                level.addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0, 0.0d);
            }
        });
    }
}
