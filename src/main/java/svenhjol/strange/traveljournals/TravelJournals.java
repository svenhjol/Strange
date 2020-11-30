package svenhjol.strange.traveljournals;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.iface.Module;
import svenhjol.charm.event.LoadWorldCallback;
import svenhjol.strange.Strange;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

@Module(mod = Strange.MOD_ID, client = TravelJournalsClient.class)
public class TravelJournals extends CharmModule {
    public static final int MAX_ENTRIES = 30;
    public static final int MAX_NAME_LENGTH = 24;
    public static final int SCREENSHOT_DISTANCE = 10;

    public static final Identifier MSG_SERVER_OPEN_JOURNAL = new Identifier(Strange.MOD_ID, "server_open_journal");
    public static final Identifier MSG_SERVER_ADD_ENTRY = new Identifier(Strange.MOD_ID, "server_add_entry");
    public static final Identifier MSG_SERVER_UPDATE_ENTRY = new Identifier(Strange.MOD_ID, "server_update_entry");
    public static final Identifier MSG_SERVER_DELETE_ENTRY = new Identifier(Strange.MOD_ID, "server_delete_entry");
    public static final Identifier MSG_CLIENT_RECEIVE_ENTRY = new Identifier(Strange.MOD_ID, "client_receive_entry");
    public static final Identifier MSG_CLIENT_RECEIVE_ENTRIES = new Identifier(Strange.MOD_ID, "client_receive_entries");

    public static TravelJournalItem TRAVEL_JOURNAL;

    private static TravelJournalManager travelJournalManager;

    @Override
    public void register() {
        TRAVEL_JOURNAL = new TravelJournalItem(this);
    }

    @Override
    public void init() {
        ServerSidePacketRegistry.INSTANCE.register(MSG_SERVER_OPEN_JOURNAL, this::handleServerOpenJournal);
        ServerSidePacketRegistry.INSTANCE.register(MSG_SERVER_ADD_ENTRY, this::handleServerAddEntry);
        ServerSidePacketRegistry.INSTANCE.register(MSG_SERVER_UPDATE_ENTRY, this::handleServerUpdateEntry);
        ServerSidePacketRegistry.INSTANCE.register(MSG_SERVER_DELETE_ENTRY, this::handleServerDeleteEntry);


        // load travel journal manager when world starts
        LoadWorldCallback.EVENT.register(this::loadTravelJournalManager);
    }

    public static Optional<TravelJournalManager> getTravelJournalManager() {
        return travelJournalManager != null ? Optional.of(travelJournalManager) : Optional.empty();
    }

    private void loadTravelJournalManager(MinecraftServer server) {
        ServerWorld overworld = server.getWorld(World.OVERWORLD);

        if (overworld == null) {
            Charm.LOG.warn("Overworld is null, cannot load persistent state manager");
            return;
        }

        PersistentStateManager stateManager = overworld.getPersistentStateManager();
        travelJournalManager = stateManager.getOrCreate(() -> new TravelJournalManager(overworld), TravelJournalManager.nameFor(overworld.getDimension()));
    }

    private void handleServerOpenJournal(PacketContext context, PacketByteBuf data) {
        processClientPacket(context, (player, manager) -> {
            ListTag listTag = manager.serializePlayerEntries(player.getUuid());
            sendJournalEntriesPacket(player, listTag);
        });
    }

    private void handleServerUpdateEntry(PacketContext context, PacketByteBuf data) {
        CompoundTag entry = data.readCompoundTag();
        if (entry == null || entry.isEmpty())
            return;

        processClientPacket(context, (player, manager) -> {
            JournalEntry updated = manager.updateJournalEntry(player, new JournalEntry(entry));
            if (updated == null)
                return;

            sendJournalEntryPacket(player, updated.toTag());
        });
    }

    private void handleServerDeleteEntry(PacketContext context, PacketByteBuf data) {
        CompoundTag entry = data.readCompoundTag();
        if (entry == null || entry.isEmpty())
            return;

        processClientPacket(context, (player, manager) -> {
            manager.deleteJournalEntry(player, new JournalEntry(entry));
            ListTag listTag = manager.serializePlayerEntries(player.getUuid());
            sendJournalEntriesPacket(player, listTag);
        });
    }

    private void handleServerAddEntry(PacketContext context, PacketByteBuf data) {
        processClientPacket(context, (player, manager) -> {
            JournalEntry entry = manager.addJournalEntry(player);
            if (entry == null) {
                Charm.LOG.warn("Failed to create a new journal entry, doing nothing");
                return;
            }

            sendJournalEntryPacket(player, entry.toTag());
        });
    }

    private void processClientPacket(PacketContext context, BiConsumer<ServerPlayerEntity, TravelJournalManager> callback) {
        context.getTaskQueue().execute(() -> {
            ServerPlayerEntity player = (ServerPlayerEntity)context.getPlayer();
            if (player == null)
                return;

            Optional<TravelJournalManager> travelJournalManager = getTravelJournalManager();
            travelJournalManager.ifPresent(manager -> callback.accept(player, manager));
        });
    }

    private void sendJournalEntriesPacket(ServerPlayerEntity player, ListTag entries) {
        UUID uuid = player.getUuid();
        CompoundTag outTag = new CompoundTag();
        outTag.put(uuid.toString(), entries);

        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeCompoundTag(outTag);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, MSG_CLIENT_RECEIVE_ENTRIES, buffer);
    }

    private void sendJournalEntryPacket(ServerPlayerEntity player, CompoundTag entry) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeCompoundTag(entry);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, MSG_CLIENT_RECEIVE_ENTRY, buffer);
    }
}
