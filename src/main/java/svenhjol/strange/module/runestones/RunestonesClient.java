package svenhjol.strange.module.runestones;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;

import java.util.HashMap;
import java.util.Map;

@ClientModule(module = Runestones.class)
public class RunestonesClient extends CharmModule {
    public static Map<Integer, String> CACHED_DESTINATION_NAMES = new HashMap<>();

    @Override
    public void register() {
        ClientPlayNetworking.registerGlobalReceiver(Runestones.MSG_CLIENT_CACHE_LEARNED_RUNES, this::handleClientCacheLearnedRunes);
        ClientPlayNetworking.registerGlobalReceiver(Runestones.MSG_CLIENT_CACHE_DESTINATION_NAMES, this::handleClientCacheDestinationNames);

        EntityRendererRegistry.INSTANCE.register(Runestones.RUNESTONE_DUST_ENTITY, RunestoneDustEntityRenderer::new);
    }

    private void handleClientCacheLearnedRunes(Minecraft client, ClientPacketListener handler, FriendlyByteBuf data, PacketSender sender) {
        int[] discoveries = data.readVarIntArray();
        client.execute(() -> RunestonesHelper.populateLearnedRunes(client.player, discoveries));
    }

    private void handleClientCacheDestinationNames(Minecraft client, ClientPacketListener handler, FriendlyByteBuf data, PacketSender sender) {
        CompoundTag inTag = data.readNbt();
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
