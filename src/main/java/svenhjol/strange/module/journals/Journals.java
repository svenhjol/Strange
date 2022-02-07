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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.api.event.PlayerLoadDataCallback;
import svenhjol.charm.api.event.PlayerSaveDataCallback;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.init.CharmAdvancements;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.api.event.QuestEvents;
import svenhjol.strange.module.journals.definition.BookmarkIconsDefinition;
import svenhjol.strange.module.journals.helper.JournalHelper;
import svenhjol.strange.module.journals.network.*;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.Quests;

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

    public static final ResourceLocation TRIGGER_LEARN_RUNE = new ResourceLocation(Strange.MOD_ID, "learn_rune");
    public static final ResourceLocation TRIGGER_LEARN_ALL_RUNES = new ResourceLocation(Strange.MOD_ID, "learn_all_runes");
    public static final ResourceLocation TRIGGER_OPEN_JOURNAL = new ResourceLocation(Strange.MOD_ID, "open_journal");
    public static final ResourceLocation TRIGGER_MAKE_BOOKMARK = new ResourceLocation(Strange.MOD_ID, "make_bookmark");

    public static ServerSendJournal SERVER_SEND_JOURNAL;
    public static ServerSendBookmarkIcons SERVER_SEND_BOOKMARK_ICONS;
    public static ServerSendPage SERVER_SEND_PAGE;
    public static ServerSendHint SERVER_SEND_HINT;
    public static ServerReceiveMakeMap SERVER_RECEIVE_MAKE_MAP;
    public static ServerReceiveOpenJournal SERVER_RECEIVE_OPEN_JOURNAL;

    public static SoundEvent SCREENSHOT_SOUND;

    @Config(name = "Show bookmark coordinates", description = "If true, shows the bookmark's XYZ coordinates.")
    public static boolean showBookmarkCoordinates = true;

    @Override
    public void register() {
        SCREENSHOT_SOUND = CommonRegistry.sound(new ResourceLocation(Strange.MOD_ID, "screenshot"));
    }

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
        SERVER_SEND_HINT = new ServerSendHint();
        SERVER_RECEIVE_MAKE_MAP = new ServerReceiveMakeMap();
        SERVER_RECEIVE_OPEN_JOURNAL = new ServerReceiveOpenJournal();
    }

    public static Optional<JournalData> getJournal(Player player) {
        return Optional.ofNullable(playerJournals.get(player.getUUID()));
    }

    public static void triggerOpenJournal(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_OPEN_JOURNAL);
    }

    public static void triggerMakeBookmark(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_MAKE_BOOKMARK);
    }

    public static void triggerLearnRune(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_LEARN_RUNE);
    }

    public static void triggerLearnAllRunes(ServerPlayer player) {
        CharmAdvancements.ACTION_PERFORMED.trigger(player, TRIGGER_LEARN_ALL_RUNES);
    }

    private void handleQuestComplete(Quest quest, ServerPlayer player) {
        // Quest tiers are tied to rune tiers.
        // Each quest in a tier will reward the player with a single rune from the equivalent rune tier.
        // Once the player has exhausted that tier's runes, higher level quests will reward new ones.
        if (!Quests.rewardRunes) return;

        var journal = Journals.getJournal(player).orElse(null);
        if (journal == null) return;

        var tier = quest.getTier();
        JournalHelper.tryLearnRune(tier, journal, player);
    }

    private void handlePlayerJoin(ServerGamePacketListenerImpl listener, PacketSender sender, MinecraftServer server) {
        var player = listener.getPlayer();
        SERVER_SEND_JOURNAL.send(player);
        SERVER_SEND_BOOKMARK_ICONS.send(player);

        getJournal(player).ifPresent(journal -> {
            if (!journal.hasOpened()) {
                SERVER_SEND_HINT.send(player); // sends the "open journal" hint once per client login
            }
        });
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
