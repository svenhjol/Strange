package svenhjol.strange.module.writing_desks;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;

@ClientModule(module = WritingDesks.class)
public class WritingDesksClient extends CharmModule {
    public static boolean validRunes = false;

    @Override
    public void runWhenEnabled() {
        ScreenRegistry.register(WritingDesks.MENU, WritingDeskScreen::new);
        ClientPlayNetworking.registerGlobalReceiver(WritingDesks.MSG_CLIENT_VALID_RUNES, this::handleValidRunes);
    }

    private void handleValidRunes(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        boolean isValid = buffer.readBoolean();
        client.execute(() -> validRunes = isValid);
    }
}
