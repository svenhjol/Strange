package svenhjol.strange.module.journals;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.event.PlayerDieCallback;
import svenhjol.charm.event.PlayerLoadDataCallback;
import svenhjol.charm.event.PlayerSaveDataCallback;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.event.QuestEvents;
import svenhjol.strange.helper.LootHelper;
import svenhjol.strange.helper.MapHelper;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.Quests;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

@CommonModule(mod = Strange.MOD_ID)
public class Journals extends CharmModule {
    public static final ResourceLocation MSG_SERVER_OPEN_JOURNAL = new ResourceLocation(Strange.MOD_ID, "server_open_journal");
    public static final ResourceLocation MSG_SERVER_SYNC_JOURNAL = new ResourceLocation(Strange.MOD_ID, "server_sync_journal");
    public static final ResourceLocation MSG_SERVER_ADD_BOOKMARK = new ResourceLocation(Strange.MOD_ID, "server_add_bookmark");
    public static final ResourceLocation MSG_SERVER_UPDATE_BOOKMARK = new ResourceLocation(Strange.MOD_ID, "server_update_bookmark");
    public static final ResourceLocation MSG_SERVER_DELETE_BOOKMARK = new ResourceLocation(Strange.MOD_ID, "server_delete_bookmark");
    public static final ResourceLocation MSG_SERVER_MAKE_MAP = new ResourceLocation(Strange.MOD_ID, "server_make_map");
    public static final ResourceLocation MSG_CLIENT_OPEN_JOURNAL = new ResourceLocation(Strange.MOD_ID, "client_open_journal");
    public static final ResourceLocation MSG_CLIENT_SYNC_JOURNAL = new ResourceLocation(Strange.MOD_ID, "client_sync_journal");
    public static final ResourceLocation MSG_CLIENT_OPEN_BOOKMARK = new ResourceLocation(Strange.MOD_ID, "client_open_bookmark");

    public static final List<ItemLike> bookmarkIcons = new LinkedList<>();
    private static final Map<UUID, JournalData> journal = new HashMap<>();

    @Config(name = "Enable keybind", description = "If true, you can use a key to open the journal (defaults to 'J').")
    public static boolean enableKeybind = true;

    @Override
    public void runWhenEnabled() {
        PlayerLoadDataCallback.EVENT.register(this::handlePlayerLoadData);
        PlayerSaveDataCallback.EVENT.register(this::handlePlayerSaveData);
        PlayerDieCallback.EVENT.register(this::handlePlayerDeath);
        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        QuestEvents.COMPLETE.register(this::handleQuestComplete);

        ServerPlayNetworking.registerGlobalReceiver(MSG_SERVER_OPEN_JOURNAL, this::handleOpenJournal);
        ServerPlayNetworking.registerGlobalReceiver(MSG_SERVER_SYNC_JOURNAL, this::handleSyncJournal);
        ServerPlayNetworking.registerGlobalReceiver(MSG_SERVER_ADD_BOOKMARK, this::handleAddBookmark);
        ServerPlayNetworking.registerGlobalReceiver(MSG_SERVER_UPDATE_BOOKMARK, this::handleUpdateBookmark);
        ServerPlayNetworking.registerGlobalReceiver(MSG_SERVER_DELETE_BOOKMARK, this::handleDeleteBookmark);
        ServerPlayNetworking.registerGlobalReceiver(MSG_SERVER_MAKE_MAP, this::handleMakeMap);
    }

    private void handlePlayerLoadData(Player player, File playerDataDir) {
        UUID uuid = player.getUUID();
        CompoundTag nbt = PlayerLoadDataCallback.readFile(new File(playerDataDir + "/" + uuid.toString() + "_strange_journal.dat"));
        journal.put(uuid, JournalData.fromNbt(player, nbt));
    }

    private void handlePlayerSaveData(Player player, File playerDataDir) {
        UUID uuid = player.getUUID();
        if (journal.containsKey(uuid)) {
            CompoundTag nbt = new CompoundTag();
            journal.get(uuid).toNbt(nbt);
            PlayerSaveDataCallback.writeFile(new File(playerDataDir + "/" + uuid.toString() + "_strange_journal.dat"), nbt);
        }
    }

    private void handleOpenJournal(MinecraftServer server, ServerPlayer player, ServerGamePacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        Page page = buffer.readEnum(Page.class);

        processPacketFromClient(server, player,
            data -> NetworkHelper.sendPacketToClient(player, MSG_CLIENT_OPEN_JOURNAL, buf -> {
                buf.writeNbt(data.toNbt());
                buf.writeEnum(page);
            }));
    }

    private void handleSyncJournal(MinecraftServer server, ServerPlayer player, ServerGamePacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        processPacketFromClient(server, player,
            data -> NetworkHelper.sendPacketToClient(player, MSG_CLIENT_SYNC_JOURNAL, buf -> buf.writeNbt(data.toNbt())));
    }

    private void handleAddBookmark(MinecraftServer server, ServerPlayer player, ServerGamePacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        processPacketFromClient(server, player, data -> {
            JournalBookmark newBookmark = data.addBookmark(player.level, player.blockPosition());
            NetworkHelper.sendPacketToClient(player, MSG_CLIENT_OPEN_BOOKMARK, buf -> buf.writeNbt(newBookmark.toNbt(new CompoundTag())));
        });
    }

    private void handleUpdateBookmark(MinecraftServer server, ServerPlayer player, ServerGamePacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        CompoundTag tag = buffer.readNbt();
        if (tag == null) return; // don't handle

        processPacketFromClient(server, player, data -> {
            JournalBookmark bookmark = JournalBookmark.fromNbt(tag);
            data.updateBookmark(bookmark);
        });
    }

    private void handleDeleteBookmark(MinecraftServer server, ServerPlayer player, ServerGamePacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        CompoundTag tag = buffer.readNbt();
        if (tag == null) return; // don't handle

        processPacketFromClient(server, player, data -> {
            JournalBookmark bookmark = JournalBookmark.fromNbt(tag);
            data.deleteBookmark(bookmark);
        });
    }

    private void handleMakeMap(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        CompoundTag tag = buffer.readNbt();
        if (tag == null) return;

        processPacketFromClient(server, player, data -> {
            JournalBookmark bookmark = JournalBookmark.fromNbt(tag);
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

    private void handleQuestComplete(Quest quest, ServerPlayer player) {
        if (!Strange.LOADER.isEnabled(Quests.class) || !Strange.LOADER.isEnabled(Knowledge.class)) {
            return; // don't handle if quests and knowledge are unavailable
        }

        int tier = quest.getTier();
        JournalData journal = Journals.getJournalData(player).orElseThrow();
        boolean learned = JournalHelper.learnNextLearnableRune(tier, journal);

        if (learned) {
            player.displayClientMessage(new TranslatableComponent("gui.strange.journal.learned_rune"), true);
        }
    }

    /**
     * Write a death entry to the bookmarks if the player dies.
     */
    private void handlePlayerDeath(ServerPlayer player, DamageSource source) {
        getJournalData(player).ifPresent(data -> data.addDeathBookmark(player.level, player.blockPosition()));
    }

    /**
     * On world load initialize things such as the bookmark icons (generated from loot table).
     */
    private void handleWorldLoad(MinecraftServer server, Level level) {
        if (level.dimension() == Level.OVERWORLD) {
            bookmarkIcons.clear();
            LootHelper.fetchItems(server, JournalLoot.BOOKMARK_ICONS).ifPresent(m -> bookmarkIcons.addAll(m.get(0)));
        }
    }

    /**
     * Convenience method to check if player is valid and callback with a valid player journal instance.
     */
    private void processPacketFromClient(MinecraftServer server, ServerPlayer player, Consumer<JournalData> callback) {
        server.execute(() -> {
            if (player == null) return;
            Journals.getJournalData(player).ifPresent(callback);
        });
    }

    public static void sendOpenJournal(ServerPlayer player, Page page) {
        getJournalData(player).ifPresent(journal -> {
            NetworkHelper.sendPacketToClient(player, MSG_CLIENT_OPEN_JOURNAL, buf -> {
                buf.writeNbt(journal.toNbt());
                buf.writeEnum(page);
            });
        });
    }

    public static void sendSyncJournal(ServerPlayer player) {
        getJournalData(player).ifPresent(journal -> {
            NetworkHelper.sendPacketToClient(player, MSG_CLIENT_SYNC_JOURNAL, buf -> {
                buf.writeNbt(journal.toNbt());
            });
        });
    }

    public static Optional<JournalData> getJournalData(Player player) {
        return Optional.ofNullable(journal.get(player.getUUID()));
    }

    public enum Page {
        HOME,
        BOOKMARKS,
        QUESTS,
        KNOWLEDGE
    }
}
