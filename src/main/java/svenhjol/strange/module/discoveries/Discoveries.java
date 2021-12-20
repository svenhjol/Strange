package svenhjol.strange.module.discoveries;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.api.network.DiscoveryMessages;

import java.util.Optional;

@CommonModule(mod = Strange.MOD_ID)
public class Discoveries extends CharmModule {
    private static DiscoveryData discoveryData;

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        ServerPlayNetworking.registerGlobalReceiver(DiscoveryMessages.SERVER_SYNC_DISCOVERIES, this::handleSyncDiscoveries);
    }

    public static Optional<DiscoveryData> getDiscoveries() {
        return Optional.ofNullable(discoveryData);
    }

    private void handleWorldLoad(MinecraftServer server, Level level) {

        // Overworld loaded first and only once.
        if (level.dimension() == Level.OVERWORLD) {
            var overworld = (ServerLevel) level;
            var storage = overworld.getDataStorage();

            discoveryData = storage.computeIfAbsent(
                tag -> DiscoveryData.load(overworld, tag),
                () -> new DiscoveryData(overworld),
                DiscoveryData.getFileId(level.dimensionType())
            );
        }
    }

    private void handleSyncDiscoveries(MinecraftServer server, ServerPlayer player, ServerGamePacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        var discoveries = getDiscoveries().orElse(null);
        if (discoveries == null) return;

        server.execute(() -> {
            var tag = new CompoundTag();
            discoveries.branch.save(tag);
            NetworkHelper.sendPacketToClient(player, DiscoveryMessages.CLIENT_SYNC_DISCOVERIES, buf -> buf.writeNbt(tag));
        });
    }

    public static void sendInteractDiscovery(ServerPlayer player, Discovery discovery) {
        NetworkHelper.sendPacketToClient(player, DiscoveryMessages.CLIENT_INTERACT_DISCOVERY, buf -> buf.writeNbt(discovery.save()));
    }

    public static void sendAddDiscovery(MinecraftServer server, Discovery discovery) {
        NetworkHelper.sendPacketToAllClients(server, DiscoveryMessages.CLIENT_ADD_DISCOVERY, buf -> buf.writeNbt(discovery.save()));
    }
}
