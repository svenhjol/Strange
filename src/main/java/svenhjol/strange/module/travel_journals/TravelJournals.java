package svenhjol.strange.module.travel_journals;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.storage.DimensionDataStorage;
import svenhjol.charm.Charm;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.annotation.Module;
import svenhjol.charm.event.LoadServerFinishCallback;
import svenhjol.charm.event.PlayerDropInventoryCallback;
import svenhjol.charm.handler.ModuleHandler;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.MapHelper;
import svenhjol.charm.helper.PlayerHelper;
import svenhjol.charm.module.CharmModule;
import svenhjol.charm.module.bookcases.Bookcases;
import svenhjol.strange.Strange;
import svenhjol.strange.helper.TotemsHelper;
import svenhjol.strange.module.runestones.Runestones;
import svenhjol.strange.module.scrolls.Scrolls;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Module(mod = Strange.MOD_ID, client = TravelJournalsClient.class, description = "Helps keep track of interesting locations, current scrolls and learned runes.")
public class TravelJournals extends CharmModule {
    public static final int MAX_ENTRIES = 50;
    public static final int MAX_NAME_LENGTH = 32;
    public static final int SCREENSHOT_DISTANCE = 10;

    public static final ResourceLocation MSG_SERVER_OPEN_JOURNAL = new ResourceLocation(Strange.MOD_ID, "server_open_journal");
    public static final ResourceLocation MSG_SERVER_ADD_ENTRY = new ResourceLocation(Strange.MOD_ID, "server_add_entry");
    public static final ResourceLocation MSG_SERVER_UPDATE_ENTRY = new ResourceLocation(Strange.MOD_ID, "server_update_entry");
    public static final ResourceLocation MSG_SERVER_DELETE_ENTRY = new ResourceLocation(Strange.MOD_ID, "server_delete_entry");
    public static final ResourceLocation MSG_SERVER_MAKE_MAP = new ResourceLocation(Strange.MOD_ID, "server_make_map");
    public static final ResourceLocation MSG_SERVER_USE_TOTEM = new ResourceLocation(Strange.MOD_ID, "server_use_totem");
    public static final ResourceLocation MSG_CLIENT_RECEIVE_ENTRY = new ResourceLocation(Strange.MOD_ID, "client_receive_entry");
    public static final ResourceLocation MSG_CLIENT_RECEIVE_ENTRIES = new ResourceLocation(Strange.MOD_ID, "client_receive_entries");

    public static TravelJournalItem TRAVEL_JOURNAL;

    private static TravelJournalSavedData savedData;

    @Config(name = "Show coordinates", description = "If true, the coordinates and dimension are shown on the update entry screen.")
    public static boolean showCoordinates = true;

    @Config(name = "Enable keybind", description = "If true, sets a keybind for opening the travel journal (defaults to 'b').")
    public static boolean enableKeybind = true;

    @Override
    public void register() {
        TRAVEL_JOURNAL = new TravelJournalItem(this);
    }

    @Override
    public void init() {
        // load travel journal saved data when world starts
        LoadServerFinishCallback.EVENT.register(this::loadSavedData);

        // allow travel journals on Charm's bookcases
        if (ModuleHandler.enabled(Bookcases.class))
            Bookcases.validItems.add(TRAVEL_JOURNAL);

        // record death positions in travel journal
        PlayerDropInventoryCallback.EVENT.register(this::tryInterceptDropInventory);

        ServerPlayNetworking.registerGlobalReceiver(TravelJournals.MSG_SERVER_OPEN_JOURNAL, this::handleServerOpenJournal);
        ServerPlayNetworking.registerGlobalReceiver(TravelJournals.MSG_SERVER_ADD_ENTRY, this::handleServerAddEntry);
        ServerPlayNetworking.registerGlobalReceiver(TravelJournals.MSG_SERVER_UPDATE_ENTRY, this::handleServerUpdateEntry);
        ServerPlayNetworking.registerGlobalReceiver(TravelJournals.MSG_SERVER_DELETE_ENTRY, this::handleServerDeleteEntry);
        ServerPlayNetworking.registerGlobalReceiver(TravelJournals.MSG_SERVER_MAKE_MAP, this::handleServerMakeMap);
        ServerPlayNetworking.registerGlobalReceiver(TravelJournals.MSG_SERVER_USE_TOTEM, this::handleServerUseTotem);
    }

    public static Optional<TravelJournalSavedData> getSavedData() {
        return Optional.ofNullable(savedData);
    }

    public static void sendJournalEntriesPacket(ServerPlayer player, ListTag entries) {
        UUID uuid = player.getUUID();
        CompoundTag outTag = new CompoundTag();
        outTag.put(uuid.toString(), entries);

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeNbt(outTag);
        ServerPlayNetworking.send(player, MSG_CLIENT_RECEIVE_ENTRIES, buffer);
    }

    public static void sendJournalEntryPacket(ServerPlayer player, CompoundTag entry) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeNbt(entry);
        ServerPlayNetworking.send(player, MSG_CLIENT_RECEIVE_ENTRY, buffer);
    }

    private void loadSavedData(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);

        if (overworld == null) {
            Charm.LOG.warn("[Travel Journal] Overworld is null, cannot load saved data");
            return;
        }

        DimensionDataStorage storage = overworld.getDataStorage();
        savedData = storage.computeIfAbsent(
            (tag) -> TravelJournalSavedData.fromNbt(overworld, tag),
            () -> new TravelJournalSavedData(overworld),
            TravelJournalSavedData.nameFor(overworld.dimensionType()));

        Charm.LOG.info("[Travel Journal] Loaded travel journal saved data");
    }

    private void handleServerOpenJournal(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf data, PacketSender sender) {
        processClientPacket(server, player, savedData -> {
            ListTag listTag = savedData.serializePlayerEntries(player.getUUID());
            TravelJournals.sendJournalEntriesPacket(player, listTag);

            if (ModuleHandler.enabled(Runestones.class)) {
                Runestones.sendLearnedRunesPacket(player);
                Runestones.sendDestinationNamesPacket(player);
            }

            if (ModuleHandler.enabled(Scrolls.class)) {
                Scrolls.sendPlayerQuestsPacket(player);
            }

            // for the advancement
            CriteriaTriggers.CONSUME_ITEM.trigger(player, new ItemStack(TravelJournals.TRAVEL_JOURNAL));
        });
    }

    private void handleServerUpdateEntry(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf data, PacketSender sender) {
        CompoundTag entry = data.readNbt();
        if (entry == null || entry.isEmpty())
            return;

        processClientPacket(server, player, savedData -> {
            TravelJournalEntry updated = savedData.updateJournalEntry(player, new TravelJournalEntry(entry));
            if (updated == null)
                return;

            TravelJournals.sendJournalEntryPacket(player, updated.toNbt());
        });
    }

    private void handleServerDeleteEntry(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf data, PacketSender sender) {
        CompoundTag entry = data.readNbt();
        if (entry == null || entry.isEmpty())
            return;

        processClientPacket(server, player, savedData -> {
            savedData.deleteJournalEntry(player, new TravelJournalEntry(entry));
            ListTag listTag = savedData.serializePlayerEntries(player.getUUID());
            TravelJournals.sendJournalEntriesPacket(player, listTag);
        });
    }

    private void handleServerAddEntry(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf data, PacketSender sender) {
        processClientPacket(server, player, savedData -> {
            TravelJournalEntry entry = savedData.initJournalEntry(player);
            if (entry == null) {
                Charm.LOG.warn("Failed to create a new journal entry, doing nothing");
                return;
            }

            TravelJournals.sendJournalEntryPacket(player, entry.toNbt());
        });
    }

    private void handleServerUseTotem(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf data, PacketSender sender) {
        CompoundTag entryTag = data.readNbt();
        if (entryTag == null || entryTag.isEmpty())
            return;

        processClientPacket(server, player, savedData -> {
            // check the player has a totem
            ItemStack requiredItem = new ItemStack(Items.NETHER_STAR); // TODO: placeholder item, phase2
            int slotWithStack = PlayerHelper.getInventory(player).findSlotMatchingItem(requiredItem);
            if (slotWithStack == -1)
                return;

            // check the journal entry exists
            Optional<TravelJournalEntry> optionalEntry = savedData.getJournalEntry(player, new TravelJournalEntry(entryTag));
            if (optionalEntry.isEmpty())
                return;

            // check the player is in the right dimension for this entry
            TravelJournalEntry entry = optionalEntry.get();
            if (!DimensionHelper.isDimension(player.level, entry.dim))
                return;

            // destroy totem
            ItemStack heldTotem = PlayerHelper.getInventory(player).getItem(slotWithStack);
            TotemsHelper.destroy(player, heldTotem);

            Level world = player.level;
//            Criteria.USED_TOTEM.trigger(player, new ItemStack(TotemOfWandering.TOTEM_OF_WANDERING)); // TODO: bring this advancement back in phase2
            PlayerHelper.teleport(world, entry.pos, player);
            player.level.playSound(null, entry.pos, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8F, 1.0F);
        });
    }

    private void handleServerMakeMap(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf data, PacketSender sender) {
        CompoundTag entryTag = data.readNbt();
        if (entryTag == null || entryTag.isEmpty())
            return;

        processClientPacket(server, player, savedData -> {
            // check the player has a map
            ItemStack requiredItem = new ItemStack(Items.MAP);
            int slotWithStack = PlayerHelper.getInventory(player).findSlotMatchingItem(requiredItem);
            if (slotWithStack == -1)
                return;

            // check the journal entry exists
            Optional<TravelJournalEntry> optionalEntry = savedData.getJournalEntry(player, new TravelJournalEntry(entryTag));
            if (optionalEntry.isEmpty())
                return;

            // get the entry and reduce the map stack by 1
            TravelJournalEntry entry = optionalEntry.get();
            ItemStack heldMap = PlayerHelper.getInventory(player).getItem(slotWithStack);
            heldMap.shrink(1);

            DyeColor col = DyeColor.byId(entry.color);
            MapDecoration.Type decoration = MapDecoration.Type.TARGET_X;
            if (col == DyeColor.BLACK) decoration = MapDecoration.Type.BANNER_BLACK;
            if (col == DyeColor.BLUE) decoration = MapDecoration.Type.BANNER_BLUE;
            if (col == DyeColor.PURPLE) decoration = MapDecoration.Type.BANNER_PURPLE;
            if (col == DyeColor.RED) decoration = MapDecoration.Type.BANNER_RED;
            if (col == DyeColor.BROWN) decoration = MapDecoration.Type.BANNER_BROWN;
            if (col == DyeColor.GREEN) decoration = MapDecoration.Type.BANNER_GREEN;
            if (col == DyeColor.GRAY) decoration = MapDecoration.Type.BANNER_GRAY;

            ItemStack outMap = MapHelper.getMap((ServerLevel) player.level, entry.pos, new TranslatableComponent(entry.name), decoration, col.getFireworkColor());
            PlayerHelper.addOrDropStack(player, outMap);

            TravelJournals.sendJournalEntryPacket(player, entry.toNbt());
        });
    }

    private void processClientPacket(MinecraftServer server, ServerPlayer player, Consumer<TravelJournalSavedData> callback) {
        server.execute(() -> {
            if (player == null)
                return;

            Optional<TravelJournalSavedData> opt = TravelJournals.getSavedData();
            opt.ifPresent(callback);
        });
    }

    private InteractionResult tryInterceptDropInventory(Player player, Inventory inventory) {
        if (player.level.isClientSide)
            return InteractionResult.PASS;

        // add position to travel journal
        Optional<TravelJournalSavedData> opt = TravelJournals.getSavedData();
        opt.ifPresent(savedData -> {
            TravelJournalEntry entry = new TravelJournalEntry(
                new TranslatableComponent("item.charm.totem_of_preserving").getString(),
                player.blockPosition(),
                DimensionHelper.getDimension(player.level),
                1
            );
            savedData.addJournalEntry(player, entry);
        });

        return InteractionResult.SUCCESS;
    }
}