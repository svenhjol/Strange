package svenhjol.strange.module.runestones;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.knowledge.types.Discovery;

@ClientModule(module = Runestones.class)
public class RunestonesClient extends CharmModule {
    public static Discovery discoveryHolder = null;

    @Override
    public void register() {
        ScreenRegistry.register(Runestones.MENU, RunestoneScreen::new);
        EntityRendererRegistry.register(Runestones.RUNESTONE_DUST_ENTITY, RunestoneDustEntityRenderer::new);
    }

    @Override
    public void runWhenEnabled() {
        ClientPlayNetworking.registerGlobalReceiver(Runestones.MSG_CLIENT_SET_DESTINATION, this::handleSetDestination);
    }

    private void handleSetDestination(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        CompoundTag tag = buffer.readNbt();
        if (tag == null) {
            LogHelper.error(this.getClass(), "Could not read destination tag from buffer");
            return;
        }

        client.execute(() -> {
            discoveryHolder = Discovery.fromTag(tag);
            LogHelper.debug(this.getClass(), "Destination set from server. Runes = " + discoveryHolder.getRunes() + ", Location = " + discoveryHolder.getId());
        });
    }
}
