package svenhjol.strange.module.discoveries;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.discoveries.network.ServerSendAddDiscovery;
import svenhjol.strange.module.discoveries.network.ServerSendDiscoveries;
import svenhjol.strange.module.discoveries.network.ServerSendInteractDiscovery;

import javax.annotation.Nullable;
import java.util.Optional;

@CommonModule(mod = Strange.MOD_ID, priority = 10, description = "Catalogues discoveries found by players throughout dimensions.")
public class Discoveries extends CharmModule {
    private static @Nullable DiscoveryData discoveryData;

    public static ServerSendDiscoveries SERVER_SEND_DISCOVERIES;
    public static ServerSendAddDiscovery SERVER_SEND_ADD_DISCOVERY;
    public static ServerSendInteractDiscovery SERVER_SEND_INTERACT_DISCOVERY;

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        ServerPlayConnectionEvents.JOIN.register(this::handlePlayerJoin);

        SERVER_SEND_DISCOVERIES = new ServerSendDiscoveries();
        SERVER_SEND_ADD_DISCOVERY = new ServerSendAddDiscovery();
        SERVER_SEND_INTERACT_DISCOVERY = new ServerSendInteractDiscovery();
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

    private void handlePlayerJoin(ServerGamePacketListenerImpl listener, PacketSender sender, MinecraftServer server) {
        SERVER_SEND_DISCOVERIES.send(listener.getPlayer());
    }
}
