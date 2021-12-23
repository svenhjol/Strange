package svenhjol.strange.module.journals;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.event.PlayerLoadDataCallback;
import svenhjol.charm.event.PlayerSaveDataCallback;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.journals.definition.BookmarkIconsDefinition;
import svenhjol.strange.module.journals.helper.JournalHelper;
import svenhjol.strange.module.journals.network.ServerReceiveMakeMap;
import svenhjol.strange.module.journals.network.ServerSendBookmarkIcons;
import svenhjol.strange.module.journals.network.ServerSendJournal;
import svenhjol.strange.module.journals.network.ServerSendPage;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.Quests;
import svenhjol.strange.api.event.QuestEvents;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@CommonModule(mod = Strange.MOD_ID, description = "Keeps track of bookmarks, quests and knowledge.")
public class Journals extends CharmModule {
    private static final String BOOKMARK_ICONS_DEFINITION_FOLDER = "journals";
    private static final String BOOKMARK_ICONS_DEFINITION_FILE = "bookmark_icons.json";
    public static final List<Item> BOOKMARK_ICONS = new LinkedList<>();

    private static final String FILENAME = "strange_journal.dat";
    private static final Map<UUID, JournalData> playerJournals = new HashMap<>();

    public static ServerSendJournal SERVER_SEND_JOURNAL;
    public static ServerSendBookmarkIcons SERVER_SEND_BOOKMARK_ICONS;
    public static ServerSendPage SERVER_SEND_PAGE;
    public static ServerReceiveMakeMap SERVER_RECEIVE_MAKE_MAP;

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        ServerPlayConnectionEvents.JOIN.register(this::handlePlayerJoin);
        PlayerLoadDataCallback.EVENT.register(this::handlePlayerLoadData);
        PlayerSaveDataCallback.EVENT.register(this::handlePlayerSaveData);
        QuestEvents.COMPLETE.register(this::handleQuestComplete);

        SERVER_SEND_JOURNAL = new ServerSendJournal();
        SERVER_SEND_BOOKMARK_ICONS = new ServerSendBookmarkIcons();
        SERVER_SEND_PAGE = new ServerSendPage();
        SERVER_RECEIVE_MAKE_MAP = new ServerReceiveMakeMap();
    }

    public static Optional<JournalData> getJournal(Player player) {
        return Optional.ofNullable(playerJournals.get(player.getUUID()));
    }

    private void handleQuestComplete(Quest quest, ServerPlayer player) {
        // Quest tiers are tied to rune tiers.
        // Each quest in a tier will reward the player with a single rune from the equivalent rune tier.
        // Once the player has exhausted that tier's runes, higher level quests will reward new ones.
        if (!Quests.rewardRunes) return;

        var journal = Journals.getJournal(player).orElse(null);
        if (journal == null) return;

        var val = JournalHelper.nextLearnableRune(quest.getTier(), journal);
        if (val >= 0) {
            journal.learnRune(val);
            SERVER_SEND_JOURNAL.send(player);
        }
    }

    private void handlePlayerJoin(ServerGamePacketListenerImpl listener, PacketSender sender, MinecraftServer server) {
        var player = listener.getPlayer();
        SERVER_SEND_JOURNAL.send(player);
        SERVER_SEND_BOOKMARK_ICONS.send(player);
    }

    private void handleWorldLoad(MinecraftServer server, ServerLevel level) {
        if (level.dimension() == Level.OVERWORLD) {
            initBookmarkIcons(server);
        }
    }

    private void handlePlayerLoadData(Player player, File playerDataDir) {
        UUID uuid = player.getUUID();
        CompoundTag tag = PlayerLoadDataCallback.readFile(getDataFile(playerDataDir, uuid));
        playerJournals.put(uuid, JournalData.load(tag));
    }

    private void handlePlayerSaveData(Player player, File playerDataDir) {
        getJournal(player).ifPresent(journal -> {
            CompoundTag tag = journal.save();
            PlayerSaveDataCallback.writeFile(getDataFile(playerDataDir, player.getUUID()), tag);
        });
    }

    private File getDataFile(File playerDataDir, UUID uuid) {
        return new File(playerDataDir + "/" + uuid.toString() + "_" + FILENAME);
    }

    private void initBookmarkIcons(MinecraftServer server) {
        BOOKMARK_ICONS.clear();
        ResourceManager manager = server.getResourceManager();

        Optional<ResourceLocation> resource = manager
            .listResources(BOOKMARK_ICONS_DEFINITION_FOLDER, f -> f.endsWith(".json"))
            .stream()
            .filter(r -> r.getPath().contains(BOOKMARK_ICONS_DEFINITION_FILE))
            .findFirst();

        if (resource.isEmpty()) return;

        try {
            BookmarkIconsDefinition definition = BookmarkIconsDefinition.deserialize(manager.getResource(resource.get()));
            List<Item> items = definition.getIcons().stream()
                .map(ResourceLocation::new)
                .map(Registry.ITEM::get)
                .dropWhile(BOOKMARK_ICONS::contains)
                .collect(Collectors.toList());

            BOOKMARK_ICONS.addAll(items);
        } catch (Exception e) {
            LogHelper.warn(getClass(), "Could not load bookmark icons definition from " + resource + ": " + e.getMessage());
        }
    }
}
