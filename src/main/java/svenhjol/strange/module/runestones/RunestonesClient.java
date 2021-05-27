package svenhjol.strange.module.runestones;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import svenhjol.charm.module.CharmClientModule;
import svenhjol.charm.module.CharmModule;

import java.util.HashMap;
import java.util.Map;

public class RunestonesClient extends CharmClientModule {
    public static Map<Integer, String> CACHED_DESTINATION_NAMES = new HashMap<>();

    public RunestonesClient(CharmModule module) {
        super(module);
    }

    @Override
    public void register() {
        ClientPlayNetworking.registerGlobalReceiver(Runestones.MSG_CLIENT_CACHE_LEARNED_RUNES, this::handleClientCacheLearnedRunes);
        ClientPlayNetworking.registerGlobalReceiver(Runestones.MSG_CLIENT_CACHE_DESTINATION_NAMES, this::handleClientCacheDestinationNames);

        EntityRendererRegistry.INSTANCE.register(Runestones.RUNESTONE_DUST_ENTITY, RunestoneDustEntityRenderer::new);
    }

    private void handleClientCacheLearnedRunes(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf data, PacketSender sender) {
        int[] discoveries = data.readIntArray();
        client.execute(() -> RunestonesHelper.populateLearnedRunes(client.player, discoveries));
    }

    private void handleClientCacheDestinationNames(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf data, PacketSender sender) {
        NbtCompound inTag = data.readNbt();
        if (inTag == null || inTag.isEmpty())
            return;

        client.execute(() -> {
            CACHED_DESTINATION_NAMES.clear();

            for (int i = 0; i < RunestonesHelper.NUMBER_OF_RUNES; i++) {
                String name = inTag.getString(String.valueOf(i));
                if (!name.isEmpty())
                    CACHED_DESTINATION_NAMES.put(i, name);
            }
        });
    }
}
