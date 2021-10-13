package svenhjol.strange.module.runestones;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.knowledge.Destination;

@ClientModule(module = Runestones.class)
public class RunestonesClient extends CharmModule {
    public static Destination activeDestination = null;

    @Override
    public void runWhenEnabled() {
        ScreenRegistry.register(Runestones.MENU, RunestoneScreen::new);
        ClientPlayNetworking.registerGlobalReceiver(Runestones.MSG_CLIENT_SET_ACTIVE_DESTINATION, this::handleSetActiveDestination);
    }

    private void handleSetActiveDestination(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        CompoundTag tag = buffer.readNbt();
        if (tag == null) {
            LogHelper.error(this.getClass(), "Could not read destination tag from buffer");
            return;
        }

        client.execute(() -> {
            activeDestination = Destination.fromTag(tag);
            LogHelper.debug(this.getClass(), "ActiveDestination set from server");
        });
    }
}
