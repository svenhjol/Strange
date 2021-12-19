package svenhjol.strange.module.journals2;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.event.PlayerLoadDataCallback;
import svenhjol.charm.event.PlayerSaveDataCallback;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.api.network.JournalMessages;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@CommonModule(mod = Strange.MOD_ID)
public class Journals2 extends CharmModule {
    private static final String FILENAME = "strange_journal2.dat";
    private static final Map<UUID, Journal2Data> playerJournals = new HashMap<>();

    @Override
    public void runWhenEnabled() {
        PlayerLoadDataCallback.EVENT.register(this::handlePlayerLoadData);
        PlayerSaveDataCallback.EVENT.register(this::handlePlayerSaveData);
        ServerPlayNetworking.registerGlobalReceiver(JournalMessages.SERVER_SYNC_JOURNAL, this::handleSyncJournal);
    }

    private void handlePlayerLoadData(Player player, File playerDataDir) {
        UUID uuid = player.getUUID();
        CompoundTag tag = PlayerLoadDataCallback.readFile(getDataFile(playerDataDir, uuid));
        playerJournals.put(uuid, Journal2Data.load(tag));
    }

    private void handlePlayerSaveData(Player player, File playerDataDir) {
        getJournal(player).ifPresent(journal -> {
            CompoundTag tag = journal.save();
            PlayerSaveDataCallback.writeFile(getDataFile(playerDataDir, player.getUUID()), tag);
        });
    }

    private void handleSyncJournal(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        getJournal(player).ifPresent(journal -> {
            CompoundTag tag = journal.save();
            NetworkHelper.sendPacketToClient(player, JournalMessages.CLIENT_SYNC_JOURNAL, buf -> buf.writeNbt(tag));
        });
    }

    private File getDataFile(File playerDataDir, UUID uuid) {
        return new File(playerDataDir + "/" + uuid.toString() + "_" + FILENAME);
    }

    public static Optional<Journal2Data> getJournal(Player player) {
        return Optional.ofNullable(playerJournals.get(player.getUUID()));
    }
}
