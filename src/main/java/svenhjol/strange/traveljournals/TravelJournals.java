package svenhjol.strange.traveljournals;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.charm.event.LoadWorldCallback;
import svenhjol.charm.module.Bookcases;
import svenhjol.strange.Strange;

import java.util.Optional;

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
            Bookcases.validItems.add(TravelJournalItem.class);

        TravelJournalsServer server = new TravelJournalsServer();
        server.init();
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
}
