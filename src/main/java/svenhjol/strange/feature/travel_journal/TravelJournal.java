package svenhjol.strange.feature.travel_journal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.charmony.event.PlayerLoadDataCallback;
import svenhjol.charmony.event.PlayerSaveDataCallback;
import svenhjol.charmony_api.event.EntityJoinEvent;
import svenhjol.charmony_api.event.PlayerLoadDataEvent;
import svenhjol.charmony_api.event.PlayerSaveDataEvent;
import svenhjol.strange.feature.travel_journal.Bookmarks.AddBookmarkResult;
import svenhjol.strange.feature.travel_journal.TravelJournalNetwork.MakeNewBookmark;
import svenhjol.strange.feature.travel_journal.TravelJournalNetwork.NotifyNewBookmarkResult;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TravelJournal extends CommonFeature {
    public static final String BOOKMARKS_TAG = "bookmarks";
    public static final String LEARNED_TAG = "learned";
    public static final Map<UUID, Bookmarks> BOOKMARKS = new HashMap<>();
    public static final Map<UUID, Learned> LEARNED = new HashMap<>();

    @Override
    public void register() {
        var registry = mod().registry();
        TravelJournalNetwork.register(registry);
    }

    @Override
    public void runWhenEnabled() {
        PlayerLoadDataEvent.INSTANCE.handle(this::handlePlayerLoadData);
        PlayerSaveDataEvent.INSTANCE.handle(this::handlePlayerSaveData);
        EntityJoinEvent.INSTANCE.handle(this::handleEntityJoin);
    }
    public static Optional<Learned> getLearned(Player player) {
        return Optional.ofNullable(LEARNED.get(player.getUUID()));
    }

    public static Optional<Bookmarks> getBookmarks(Player player) {
        return Optional.ofNullable(BOOKMARKS.get(player.getUUID()));
    }

    public static void syncTravelJournal(ServerPlayer player) {
        syncLearned(player);
        syncBookmarks(player);
    }

    public static void syncLearned(ServerPlayer player) {
        getLearned(player).ifPresent(
            learned -> TravelJournalNetwork.SyncLearned.send(player, learned));
    }

    public static void syncBookmarks(ServerPlayer player) {
        getBookmarks(player).ifPresent(
            bookmarks -> TravelJournalNetwork.SyncBookmarks.send(player, bookmarks));
    }

    private void handleEntityJoin(Entity entity, Level level) {
        if (!level.isClientSide && entity instanceof ServerPlayer serverPlayer) {
            syncTravelJournal(serverPlayer);
        }
    }

    private void handlePlayerSaveData(Player player, File file) {
        var uuid = player.getUUID();
        var tag = new CompoundTag();

        getLearned(player).ifPresent(learned -> tag.put(LEARNED_TAG, learned.save()));
        getBookmarks(player).ifPresent(bookmarks -> tag.put(BOOKMARKS_TAG, bookmarks.save()));

        PlayerSaveDataCallback.writeFile(getDataFile(file, uuid), tag);
    }

    private void handlePlayerLoadData(Player player, File file) {
        var uuid = player.getUUID();
        var tag = PlayerLoadDataCallback.readFile(getDataFile(file, uuid));

        var learned = Learned.load(tag.getCompound(LEARNED_TAG));
        var bookmarks = Bookmarks.load(tag.getCompound(BOOKMARKS_TAG));

        LEARNED.put(uuid, learned);
        BOOKMARKS.put(uuid, bookmarks);
    }

    private File getDataFile(File playerDataDir, UUID uuid) {
        return new File(playerDataDir + "/" + uuid.toString() + "_strange_travel_journal.dat");
    }

    public static void handleMakeNewBookmark(MakeNewBookmark message, Player player) {
        getBookmarks(player).ifPresent(bookmarks -> {
            var serverPlayer = (ServerPlayer)player;
            var result = bookmarks.add(Bookmark.playerDefault(player));

            if (result == AddBookmarkResult.SUCCESS) {
                syncBookmarks(serverPlayer);
            }

            NotifyNewBookmarkResult.send(serverPlayer, result);
        });
    }
}
