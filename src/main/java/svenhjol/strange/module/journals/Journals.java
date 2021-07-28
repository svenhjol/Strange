package svenhjol.strange.module.journals;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.event.PlayerDieCallback;
import svenhjol.charm.event.PlayerLoadDataCallback;
import svenhjol.charm.event.PlayerSaveDataCallback;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.journals.data.JournalLocation;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@CommonModule(mod = Strange.MOD_ID)
public class Journals extends CharmModule {
    public static final ResourceLocation MSG_SERVER_OPEN_JOURNAL = new ResourceLocation(Strange.MOD_ID, "server_open_journal");
    public static final ResourceLocation MSG_SERVER_SYNC_JOURNAL = new ResourceLocation(Strange.MOD_ID, "server_sync_journal");
    public static final ResourceLocation MSG_SERVER_ADD_LOCATION = new ResourceLocation(Strange.MOD_ID, "server_add_location");
    public static final ResourceLocation MSG_CLIENT_OPEN_JOURNAL = new ResourceLocation(Strange.MOD_ID, "client_open_journal");
    public static final ResourceLocation MSG_CLIENT_SYNC_JOURNAL = new ResourceLocation(Strange.MOD_ID, "client_sync_journal");
    public static final ResourceLocation MSG_CLIENT_OPEN_LOCATION = new ResourceLocation(Strange.MOD_ID, "client_open_location");

    private static final Map<UUID, JournalsData> playerData = new HashMap<>();

    @Config(name = "Enable keybind", description = "If true, you can use a key to open the journal (defaults to 'J').")
    public static boolean enableKeybind = true;

    @Override
    public void register() {

    }

    @Override
    public void runWhenEnabled() {
        PlayerLoadDataCallback.EVENT.register(this::handlePlayerLoadData);
        PlayerSaveDataCallback.EVENT.register(this::handlePlayerSaveData);
        PlayerDieCallback.EVENT.register(this::handlePlayerDeath);

        ServerPlayNetworking.registerGlobalReceiver(MSG_SERVER_OPEN_JOURNAL, this::handleOpenJournal);
        ServerPlayNetworking.registerGlobalReceiver(MSG_SERVER_ADD_LOCATION, this::handleAddLocation);
    }

    private void handlePlayerLoadData(Player player, File playerDataDir) {
        UUID uuid = player.getUUID();
        CompoundTag nbt = PlayerLoadDataCallback.readFile(new File(playerDataDir + "/" + uuid.toString() + "_strange_journal.dat"));
        playerData.put(uuid, JournalsData.fromNbt(player, nbt));
    }

    private void handlePlayerSaveData(Player player, File playerDataDir) {
        UUID uuid = player.getUUID();
        if (playerData.containsKey(uuid)) {
            CompoundTag nbt = new CompoundTag();
            playerData.get(uuid).toNbt(nbt);
            PlayerSaveDataCallback.writeFile(new File(playerDataDir + "/" + uuid.toString() + "_strange_journal.dat"), nbt);
        }
    }

    private void handleOpenJournal(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buffer, PacketSender sender) {
        Page page = buffer.readEnum(Page.class);

        processPacketFromClient(server, player,
            data -> NetworkHelper.sendPacketToClient(player, MSG_CLIENT_OPEN_JOURNAL, buf -> {
                buf.writeNbt(data.toNbt());
                buf.writeEnum(page);
            }));
    }

    private void handleSyncJournal(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buffer, PacketSender sender) {
        processPacketFromClient(server, player,
            data -> NetworkHelper.sendPacketToClient(player, MSG_CLIENT_SYNC_JOURNAL, buf -> buf.writeNbt(data.toNbt())));
    }

    private void handleAddLocation(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buffer, PacketSender sender) {
        processPacketFromClient(server, player, data -> {
            JournalLocation newLocation = data.addLocation(player.level, player.blockPosition());
            NetworkHelper.sendPacketToClient(player, MSG_CLIENT_OPEN_LOCATION, buf -> buf.writeNbt(newLocation.toNbt(new CompoundTag())));
        });
    }

    private void handlePlayerDeath(ServerPlayer player, DamageSource source) {
        getPlayerData(player).ifPresent(data ->
            data.addDeathLocation(player.level, player.blockPosition()));
    }

    private void processPacketFromClient(MinecraftServer server, ServerPlayer player, Consumer<JournalsData> callback) {
        server.execute(() -> {
            if (player == null) return;
            Journals.getPlayerData(player).ifPresent(callback);
        });
    }

    public static Optional<JournalsData> getPlayerData(Player player) {
        return Optional.ofNullable(playerData.get(player.getUUID()));
    }

    public enum Page {
        HOME,
        LOCATIONS,
        KNOWLEDGE
    }
}