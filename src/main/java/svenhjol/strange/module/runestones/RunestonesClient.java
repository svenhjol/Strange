package svenhjol.strange.module.runestones;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.NetworkHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.api.network.RunestoneMessages;

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

    }
}
