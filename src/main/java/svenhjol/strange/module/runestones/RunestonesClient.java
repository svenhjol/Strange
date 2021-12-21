package svenhjol.strange.module.runestones;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.api.network.RunestoneMessages;
import svenhjol.strange.helper.NbtHelper;
import svenhjol.strange.module.runes.Tier;

import java.util.HashMap;
import java.util.stream.Collectors;

@ClientModule(module = Runestones.class)
public class RunestonesClient extends CharmModule {

    @Override
    public void register() {
        ClientPlayConnectionEvents.JOIN.register(this::handleClientJoin);
        ScreenRegistry.register(Runestones.MENU, RunestoneScreen::new);
        EntityRendererRegistry.register(Runestones.RUNESTONE_DUST_ENTITY, RunestoneDustEntityRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(RunestoneMessages.CLIENT_SYNC_RUNESTONE_ITEMS, this::handleSyncItems);
//        ClientPlayNetworking.registerGlobalReceiver(RunestoneMessages.CLIENT_SYNC_RUNESTONE_CLUES, this::handleSyncClues);
    }

    private void handleClientJoin(ClientPacketListener listener, PacketSender sender, Minecraft minecraft) {
        NetworkHelper.sendEmptyPacketToServer(RunestoneMessages.SERVER_SYNC_RUNESTONE_ITEMS);
        NetworkHelper.sendEmptyPacketToServer(RunestoneMessages.SERVER_SYNC_RUNESTONE_CLUES);
    }

    private void handleSyncItems(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        var tag = buffer.readNbt();
        if (tag == null) return;

        client.execute(() -> {
            Runestones.ITEMS.clear();
            int count = 0;

            for (String d : tag.getAllKeys()) {
                var dimension = new ResourceLocation(d);
                var tiers = tag.getCompound(d);

                for (String t : tiers.getAllKeys()) {
                    var tier = Tier.byName(t);
                    var items = NbtHelper.unpackStrings(tiers.getCompound(t)).stream()
                        .map(ResourceLocation::new)
                        .map(Registry.ITEM::get)
                        .collect(Collectors.toList());

                    count += items.size();
                    Runestones.ITEMS.computeIfAbsent(dimension, h -> new HashMap<>()).put(tier, items);
                }
            }
            LogHelper.debug(getClass(), "Received " + count + " runestone items from server.");
        });
    }
}
