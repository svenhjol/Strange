package svenhjol.strange.module.discoveries;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.api.network.DiscoveryMessages;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

@ClientModule(module = Discoveries.class)
public class DiscoveriesClient extends CharmModule {
    public static @Nullable DiscoveryBranch branch;
    public static @Nullable Discovery interactedWithDiscovery;

    @Override
    public void runWhenEnabled() {
        ClientPlayNetworking.registerGlobalReceiver(DiscoveryMessages.CLIENT_SYNC_DISCOVERIES, this::handleSyncDiscoveries);
        ClientPlayNetworking.registerGlobalReceiver(DiscoveryMessages.CLIENT_ADD_DISCOVERY, this::handleAddDiscovery);
        ClientPlayNetworking.registerGlobalReceiver(DiscoveryMessages.CLIENT_INTERACT_DISCOVERY, this::handleInteractDiscovery);
    }

    private void handleSyncDiscoveries(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        var tag = buffer.readNbt();
        if (tag == null) return;

        client.execute(() -> {
            branch = DiscoveryBranch.load(tag);
            LogHelper.debug(getClass(), "Received " + branch.size() + " discoveries from the server.");
        });
    }

    private void handleAddDiscovery(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        processDiscovery(client, buffer.readNbt(), (branch, discovery) -> {
            // Add new discovery to local branch.
            branch.add(discovery.getRunes(), discovery);
            LogHelper.debug(getClass(), "Received new discovery `" + discovery.getRunes() + " : " + discovery.getLocation() + "` from server.");
        });
    }

    private void handleInteractDiscovery(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        if (client.player == null) return;

        var tag = buffer.readNbt();
        if (tag == null) return;

        client.execute(() -> {
            var discovery = Discovery.load(tag);
            interactedWithDiscovery = discovery;
            LogHelper.debug(getClass(), "Received interaction with `" + discovery.getRunes() + " : " + discovery.getLocation() + "` from server.");
        });
    }

    /**
     * Convenience method to fetch local discoveries, unserialize the discovery from the server, and run a consumer using the branch.
     */
    private void processDiscovery(Minecraft client, @Nullable CompoundTag tag, BiConsumer<DiscoveryBranch, Discovery> onBranch) {
        if (client.player == null) return;
        if (DiscoveriesClient.branch == null) return;
        if (tag == null) return;

        client.execute(() -> {
            // deserialize the bookmark tag and run the consumer
            var discovery = Discovery.load(tag);
            onBranch.accept(branch, discovery);
        });
    }
}