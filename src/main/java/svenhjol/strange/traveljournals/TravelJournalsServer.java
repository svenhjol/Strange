package svenhjol.strange.traveljournals;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapIcon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import svenhjol.charm.Charm;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.helper.MapHelper;
import svenhjol.charm.base.helper.PlayerHelper;
import svenhjol.strange.runestones.Runestones;
import svenhjol.strange.runestones.RunestonesServer;
import svenhjol.strange.scrolls.Scrolls;
import svenhjol.strange.scrolls.ScrollsServer;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

public class TravelJournalsServer {
    public void init() {
        ServerSidePacketRegistry.INSTANCE.register(TravelJournals.MSG_SERVER_OPEN_JOURNAL, this::handleServerOpenJournal);
        ServerSidePacketRegistry.INSTANCE.register(TravelJournals.MSG_SERVER_ADD_ENTRY, this::handleServerAddEntry);
        ServerSidePacketRegistry.INSTANCE.register(TravelJournals.MSG_SERVER_UPDATE_ENTRY, this::handleServerUpdateEntry);
        ServerSidePacketRegistry.INSTANCE.register(TravelJournals.MSG_SERVER_DELETE_ENTRY, this::handleServerDeleteEntry);
        ServerSidePacketRegistry.INSTANCE.register(TravelJournals.MSG_SERVER_MAKE_MAP, this::handleServerMakeMap);
    }

    public static void sendJournalEntriesPacket(ServerPlayerEntity player, ListTag entries) {
        UUID uuid = player.getUuid();
        CompoundTag outTag = new CompoundTag();
        outTag.put(uuid.toString(), entries);

        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeCompoundTag(outTag);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, TravelJournals.MSG_CLIENT_RECEIVE_ENTRIES, buffer);
    }

    public static void sendJournalEntryPacket(ServerPlayerEntity player, CompoundTag entry) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeCompoundTag(entry);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, TravelJournals.MSG_CLIENT_RECEIVE_ENTRY, buffer);
    }


    private void handleServerOpenJournal(PacketContext context, PacketByteBuf data) {
        processClientPacket(context, (player, manager) -> {
            ListTag listTag = manager.serializePlayerEntries(player.getUuid());
            sendJournalEntriesPacket(player, listTag);

            if (ModuleHandler.enabled(Runestones.class)) {
                RunestonesServer.sendLearnedRunesPacket(player);
                RunestonesServer.sendDestinationNamesPacket(player);
            }

            if (ModuleHandler.enabled(Scrolls.class)) {
                ScrollsServer.sendPlayerQuestsPacket(player);
            }
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

    private void handleServerMakeMap(PacketContext context, PacketByteBuf data) {
        CompoundTag entryTag = data.readCompoundTag();
        if (entryTag == null || entryTag.isEmpty())
            return;

        processClientPacket(context, (player, manager) -> {
            ItemStack requiredItem = new ItemStack(Items.MAP);
            Optional<JournalEntry> optionalEntry = manager.getJournalEntry(player, new JournalEntry(entryTag));
            if (!optionalEntry.isPresent())
                return;

            JournalEntry entry = optionalEntry.get();

            int slotWithStack = player.inventory.method_7371(requiredItem);
            if (slotWithStack == -1)
                return;

            ItemStack heldMap = player.inventory.getStack(slotWithStack);
            heldMap.decrement(1);

            DyeColor col = DyeColor.byId(entry.color);
            MapIcon.Type decoration = MapIcon.Type.TARGET_X;
            if (col == DyeColor.BLACK) decoration = MapIcon.Type.BANNER_BLACK;
            if (col == DyeColor.BLUE) decoration = MapIcon.Type.BANNER_BLUE;
            if (col == DyeColor.PURPLE) decoration = MapIcon.Type.BANNER_PURPLE;
            if (col == DyeColor.RED) decoration = MapIcon.Type.BANNER_RED;
            if (col == DyeColor.BROWN) decoration = MapIcon.Type.BANNER_BROWN;
            if (col == DyeColor.GREEN) decoration = MapIcon.Type.BANNER_GREEN;
            if (col == DyeColor.GRAY) decoration = MapIcon.Type.BANNER_GRAY;

            ItemStack outMap = MapHelper.getMap((ServerWorld) player.world, entry.pos, new TranslatableText(entry.name), decoration, col.getFireworkColor());
            PlayerHelper.addOrDropStack(player, outMap);

            sendJournalEntryPacket(player, entry.toTag());
        });
    }

    private void processClientPacket(PacketContext context, BiConsumer<ServerPlayerEntity, TravelJournalManager> callback) {
        context.getTaskQueue().execute(() -> {
            ServerPlayerEntity player = (ServerPlayerEntity)context.getPlayer();
            if (player == null)
                return;

            Optional<TravelJournalManager> travelJournalManager = TravelJournals.getTravelJournalManager();
            travelJournalManager.ifPresent(manager -> callback.accept(player, manager));
        });
    }
}
