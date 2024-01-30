package svenhjol.strange.feature.travel_journal;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import svenhjol.charmony.api.event.EntityJoinEvent;
import svenhjol.charmony.api.event.PlayerLoadDataEvent;
import svenhjol.charmony.api.event.PlayerSaveDataEvent;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.charmony.event.PlayerLoadDataCallback;
import svenhjol.charmony.event.PlayerSaveDataCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TravelJournal extends CommonFeature {
    public static Supplier<SoundEvent> interactSound;
    private static final List<Pair<BiConsumer<Player, CompoundTag>, BiConsumer<Player, CompoundTag>>> PLAYER_DATA_SOURCES = new ArrayList<>();
    private static final List<Consumer<ServerPlayer>> SYNC_HANDLERS = new ArrayList<>();

    @Override
    public void register() {
        var registry = mod().registry();
        interactSound = registry.soundEvent("travel_journal_interact");
    }

    @Override
    public void runWhenEnabled() {
        PlayerLoadDataEvent.INSTANCE.handle(this::handlePlayerLoadData);
        PlayerSaveDataEvent.INSTANCE.handle(this::handlePlayerSaveData);
        EntityJoinEvent.INSTANCE.handle(this::handleEntityJoin);
    }

    public static void registerPlayerDataSource(BiConsumer<Player, CompoundTag> load, BiConsumer<Player, CompoundTag> save) {
        PLAYER_DATA_SOURCES.add(Pair.of(load, save));
    }

    public static void registerSyncHandler(Consumer<ServerPlayer> syncHandler) {
        SYNC_HANDLERS.add(syncHandler);
    }

    public static void syncTravelJournal(ServerPlayer player) {
        SYNC_HANDLERS.forEach(handler -> handler.accept(player));
    }

    private void handleEntityJoin(Entity entity, Level level) {
        if (!level.isClientSide && entity instanceof ServerPlayer serverPlayer) {
            syncTravelJournal(serverPlayer);
        }
    }

    private void handlePlayerLoadData(Player player, File file) {
        var uuid = player.getUUID();
        var tag = PlayerLoadDataCallback.readFile(getDataFile(file, uuid));

        PLAYER_DATA_SOURCES.forEach(source -> source.getFirst().accept(player, tag));
    }

    private void handlePlayerSaveData(Player player, File file) {
        var uuid = player.getUUID();
        var tag = new CompoundTag();

        PLAYER_DATA_SOURCES.forEach(source -> source.getSecond().accept(player, tag));

        PlayerSaveDataCallback.writeFile(getDataFile(file, uuid), tag);
    }

    private File getDataFile(File playerDataDir, UUID uuid) {
        return new File(playerDataDir + "/" + uuid.toString() + "_strange_travel_journal.dat");
    }
}
