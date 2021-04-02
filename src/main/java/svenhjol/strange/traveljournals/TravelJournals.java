package svenhjol.strange.traveljournals;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapIcon;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.helper.DimensionHelper;
import svenhjol.charm.base.helper.MapHelper;
import svenhjol.charm.base.helper.PlayerHelper;
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.charm.event.LoadWorldCallback;
import svenhjol.charm.module.Bookcases;
import svenhjol.strange.Strange;
import svenhjol.strange.runestones.Runestones;
import svenhjol.strange.scrolls.Scrolls;
import svenhjol.strange.totems.TotemsHelper;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Module(mod = Strange.MOD_ID, client = TravelJournalsClient.class)
public class TravelJournals extends CharmModule {
    public static final int MAX_ENTRIES = 50;
    public static final int MAX_NAME_LENGTH = 32;
    public static final int SCREENSHOT_DISTANCE = 10;

    public static final Identifier MSG_SERVER_OPEN_JOURNAL = new Identifier(Strange.MOD_ID, "server_open_journal");
    public static final Identifier MSG_SERVER_ADD_ENTRY = new Identifier(Strange.MOD_ID, "server_add_entry");
    public static final Identifier MSG_SERVER_UPDATE_ENTRY = new Identifier(Strange.MOD_ID, "server_update_entry");
    public static final Identifier MSG_SERVER_DELETE_ENTRY = new Identifier(Strange.MOD_ID, "server_delete_entry");
    public static final Identifier MSG_SERVER_MAKE_MAP = new Identifier(Strange.MOD_ID, "server_make_map");
    public static final Identifier MSG_SERVER_USE_TOTEM = new Identifier(Strange.MOD_ID, "server_use_totem");
    public static final Identifier MSG_CLIENT_RECEIVE_ENTRY = new Identifier(Strange.MOD_ID, "client_receive_entry");
    public static final Identifier MSG_CLIENT_RECEIVE_ENTRIES = new Identifier(Strange.MOD_ID, "client_receive_entries");

    public static TravelJournalItem TRAVEL_JOURNAL;

    private static TravelJournalManager travelJournalManager;

    @Config(name = "Show coordinates", description = "If true, the coordinates and dimension are shown on the update entry screen.")
    public static boolean showCoordinates = true;

    @Config(name = "Enable keybind", description = "If true, sets a keybind for opening the travel journal (defaults to 'v').")
    public static boolean enableKeybind = true;

    @Override
    public void register() {
        TRAVEL_JOURNAL = new TravelJournalItem(this);
    }

    @Override
    public void init() {
        // load travel journal manager when world starts
        LoadWorldCallback.EVENT.register(this::loadTravelJournalManager);

        // allow travel journals on Charm's bookcases
        if (ModuleHandler.enabled(Bookcases.class))
            Bookcases.validItems.add(TRAVEL_JOURNAL);

        ServerPlayNetworking.registerGlobalReceiver(TravelJournals.MSG_SERVER_OPEN_JOURNAL, this::handleServerOpenJournal);
        ServerPlayNetworking.registerGlobalReceiver(TravelJournals.MSG_SERVER_ADD_ENTRY, this::handleServerAddEntry);
        ServerPlayNetworking.registerGlobalReceiver(TravelJournals.MSG_SERVER_UPDATE_ENTRY, this::handleServerUpdateEntry);
        ServerPlayNetworking.registerGlobalReceiver(TravelJournals.MSG_SERVER_DELETE_ENTRY, this::handleServerDeleteEntry);
        ServerPlayNetworking.registerGlobalReceiver(TravelJournals.MSG_SERVER_MAKE_MAP, this::handleServerMakeMap);
        ServerPlayNetworking.registerGlobalReceiver(TravelJournals.MSG_SERVER_USE_TOTEM, this::handleServerUseTotem);
    }

    public static Optional<TravelJournalManager> getTravelJournalManager() {
        return travelJournalManager != null ? Optional.of(travelJournalManager) : Optional.empty();
    }

    public static void sendJournalEntriesPacket(ServerPlayerEntity player, NbtList entries) {
        UUID uuid = player.getUuid();
        NbtCompound outTag = new NbtCompound();
        outTag.put(uuid.toString(), entries);

        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeCompound(outTag);
        ServerPlayNetworking.send(player, MSG_CLIENT_RECEIVE_ENTRIES, buffer);
    }

    public static void sendJournalEntryPacket(ServerPlayerEntity player, NbtCompound entry) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeCompound(entry);
        ServerPlayNetworking.send(player, MSG_CLIENT_RECEIVE_ENTRY, buffer);
    }

    private void loadTravelJournalManager(MinecraftServer server) {
        ServerWorld overworld = server.getWorld(World.OVERWORLD);

        if (overworld == null) {
            Charm.LOG.warn("[Travel Journal] Overworld is null, cannot load persistent state manager");
            return;
        }

        PersistentStateManager stateManager = overworld.getPersistentStateManager();
        travelJournalManager = stateManager.getOrCreate(
            (tag) -> TravelJournalManager.fromTag(overworld, tag),
            () -> new TravelJournalManager(overworld),
            TravelJournalManager.nameFor(overworld.getDimension()));

        Charm.LOG.info("[Travel Journal] Loaded travel journal state manager");
    }


    private void handleServerOpenJournal(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf data, PacketSender sender) {
        processClientPacket(server, player, manager -> {
            NbtList listTag = manager.serializePlayerEntries(player.getUuid());
            TravelJournals.sendJournalEntriesPacket(player, listTag);

            if (ModuleHandler.enabled(Runestones.class)) {
                Runestones.sendLearnedRunesPacket(player);
                Runestones.sendDestinationNamesPacket(player);
            }

            if (ModuleHandler.enabled(Scrolls.class)) {
                Scrolls.sendPlayerQuestsPacket(player);
            }
        });
    }

    private void handleServerUpdateEntry(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf data, PacketSender sender) {
        NbtCompound entry = data.readCompound();
        if (entry == null || entry.isEmpty())
            return;

        processClientPacket(server, player, manager -> {
            JournalEntry updated = manager.updateJournalEntry(player, new JournalEntry(entry));
            if (updated == null)
                return;

            TravelJournals.sendJournalEntryPacket(player, updated.toTag());
        });
    }

    private void handleServerDeleteEntry(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf data, PacketSender sender) {
        NbtCompound entry = data.readCompound();
        if (entry == null || entry.isEmpty())
            return;

        processClientPacket(server, player, manager -> {
            manager.deleteJournalEntry(player, new JournalEntry(entry));
            NbtList listTag = manager.serializePlayerEntries(player.getUuid());
            TravelJournals.sendJournalEntriesPacket(player, listTag);
        });
    }

    private void handleServerAddEntry(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf data, PacketSender sender) {
        processClientPacket(server, player, manager -> {
            JournalEntry entry = manager.initJournalEntry(player);
            if (entry == null) {
                Charm.LOG.warn("Failed to create a new journal entry, doing nothing");
                return;
            }

            TravelJournals.sendJournalEntryPacket(player, entry.toTag());
        });
    }

    private void handleServerUseTotem(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf data, PacketSender sender) {
        NbtCompound entryTag = data.readCompound();
        if (entryTag == null || entryTag.isEmpty())
            return;

        processClientPacket(server, player, manager -> {
            // check the player has a totem
            ItemStack requiredItem = new ItemStack(Items.NETHER_STAR); // TODO: placeholder item, phase2
            int slotWithStack = PlayerHelper.getInventory(player).getSlotWithStack(requiredItem);
            if (slotWithStack == -1)
                return;

            // check the journal entry exists
            Optional<JournalEntry> optionalEntry = manager.getJournalEntry(player, new JournalEntry(entryTag));
            if (!optionalEntry.isPresent())
                return;

            // check the player is in the right dimension for this entry
            JournalEntry entry = optionalEntry.get();
            if (!DimensionHelper.isDimension(player.world, entry.dim))
                return;

            // destroy totem
            ItemStack heldTotem = PlayerHelper.getInventory(player).getStack(slotWithStack);
            TotemsHelper.destroy(player, heldTotem);

            World world = player.world;
//            Criteria.USED_TOTEM.trigger(player, new ItemStack(TotemOfWandering.TOTEM_OF_WANDERING)); // TODO: bring this advancement back in phase2
            PlayerHelper.teleport(world, entry.pos, player);
            player.world.playSound(null, entry.pos, SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 0.8F, 1.0F);
        });
    }

    private void handleServerMakeMap(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf data, PacketSender sender) {
        NbtCompound entryTag = data.readCompound();
        if (entryTag == null || entryTag.isEmpty())
            return;

        processClientPacket(server, player, manager -> {
            // check the player has a map
            ItemStack requiredItem = new ItemStack(Items.MAP);
            int slotWithStack = PlayerHelper.getInventory(player).getSlotWithStack(requiredItem);
            if (slotWithStack == -1)
                return;

            // check the journal entry exists
            Optional<JournalEntry> optionalEntry = manager.getJournalEntry(player, new JournalEntry(entryTag));
            if (!optionalEntry.isPresent())
                return;

            // get the entry and reduce the map stack by 1
            JournalEntry entry = optionalEntry.get();
            ItemStack heldMap = PlayerHelper.getInventory(player).getStack(slotWithStack);
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

            TravelJournals.sendJournalEntryPacket(player, entry.toTag());
        });
    }

    private void processClientPacket(MinecraftServer server, ServerPlayerEntity player, Consumer<TravelJournalManager> callback) {
        server.execute(() -> {
            if (player == null)
                return;

            Optional<TravelJournalManager> travelJournalManager = TravelJournals.getTravelJournalManager();
            travelJournalManager.ifPresent(callback);
        });
    }
}
