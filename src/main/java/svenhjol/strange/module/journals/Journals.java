package svenhjol.strange.module.journals;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.event.PlayerLoadDataCallback;
import svenhjol.charm.event.PlayerSaveDataCallback;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.MapHelper;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.api.network.JournalMessages;
import svenhjol.strange.helper.NbtHelper;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.journals.PageTracker.Page;
import svenhjol.strange.module.journals.definition.BookmarkIconsDefinition;
import svenhjol.strange.module.journals.helper.JournalHelper;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.Quests;
import svenhjol.strange.module.quests.event.QuestEvents;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@CommonModule(mod = Strange.MOD_ID)
public class Journals extends CharmModule {
    private static final String BOOKMARK_ICONS_DEFINITION_FOLDER = "journals";
    private static final String BOOKMARK_ICONS_DEFINITION_FILE = "bookmark_icons.json";
    private static final List<Item> BOOKMARK_ICONS = new LinkedList<>();

    private static final String FILENAME = "strange_journal.dat";
    private static final Map<UUID, JournalData> playerJournals = new HashMap<>();

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        ServerPlayConnectionEvents.JOIN.register(this::handlePlayerJoin);
        PlayerLoadDataCallback.EVENT.register(this::handlePlayerLoadData);
        PlayerSaveDataCallback.EVENT.register(this::handlePlayerSaveData);
        ServerPlayNetworking.registerGlobalReceiver(JournalMessages.SERVER_SYNC_JOURNAL, this::handleSyncJournal);
        ServerPlayNetworking.registerGlobalReceiver(JournalMessages.SERVER_MAKE_MAP, this::handleMakeMap);
        QuestEvents.COMPLETE.register(this::handleQuestComplete);
    }

    private void handleQuestComplete(Quest quest, ServerPlayer player) {
        // Quest tiers are tied to rune tiers.
        // Each quest in a tier will reward the player with a single rune from the equivalent rune tier.
        // Once the player has exhausted that tier's runes, higher level quests will reward new ones.
        if (!Quests.rewardRunes) return;
        var val = JournalHelper.nextLearnableRune(quest.getTier());

        if (val >= 0) {
            Journals.getJournal(player).ifPresent(journal -> journal.learnRune(val));
        }
    }

    private void handlePlayerJoin(ServerGamePacketListenerImpl listener, PacketSender sender, MinecraftServer server) {
        var player = listener.getPlayer();
        sendJournal(player);
        sendBookmarkIcons(player);
    }

    private void handleWorldLoad(MinecraftServer server, ServerLevel level) {
        if (level.dimension() == Level.OVERWORLD) {
            setupBookmarkIcons(server);
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

    private void handleSyncJournal(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        server.execute(() -> sendJournal(player));
    }

    private void handleMakeMap(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        var tag = buffer.readNbt();
        if (tag == null) return;

        server.execute(() -> {
            var bookmark = Bookmark.load(tag);
            if (!DimensionHelper.isDimension(player.level, bookmark.getDimension())) return;

            Inventory inventory = player.getInventory();
            int slot = inventory.findSlotMatchingItem(new ItemStack(Items.MAP));
            if (slot == -1) return;

            BlockPos pos = bookmark.getBlockPos();
            String name = bookmark.getName();
            MapDecoration.Type decoration = MapDecoration.Type.TARGET_X;
            ItemStack map = MapHelper.create((ServerLevel)player.level, pos, new TextComponent(name), decoration, 0x000000);
            inventory.setItem(slot, map);

            ItemStack held = player.getMainHandItem().copy();
            player.setItemInHand(InteractionHand.MAIN_HAND, map);
            inventory.placeItemBackInInventory(held);
        });
    }

    private void setupBookmarkIcons(MinecraftServer server) {
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

    private File getDataFile(File playerDataDir, UUID uuid) {
        return new File(playerDataDir + "/" + uuid.toString() + "_" + FILENAME);
    }

    public static Optional<JournalData> getJournal(Player player) {
        return Optional.ofNullable(playerJournals.get(player.getUUID()));
    }

    public static void sendJournal(ServerPlayer player) {
        getJournal(player).ifPresent(journal -> {
            CompoundTag tag = journal.save();
            NetworkHelper.sendPacketToClient(player, JournalMessages.CLIENT_SYNC_JOURNAL, buf -> buf.writeNbt(tag));
        });
    }

    public static void sendBookmarkIcons(ServerPlayer player) {
        var tag = NbtHelper.packStrings(BOOKMARK_ICONS.stream()
            .map(Registry.ITEM::getKey)
            .map(ResourceLocation::toString)
            .collect(Collectors.toList()));

        NetworkHelper.sendPacketToClient(player, JournalMessages.CLIENT_SYNC_BOOKMARK_ICONS, buf -> buf.writeNbt(tag));
    }

    public static void sendOpenPage(ServerPlayer player, Page page) {
        getJournal(player).ifPresent(journal
            -> NetworkHelper.sendPacketToClient(player, JournalMessages.CLIENT_OPEN_PAGE, buf -> buf.writeEnum(page)));
    }
}
