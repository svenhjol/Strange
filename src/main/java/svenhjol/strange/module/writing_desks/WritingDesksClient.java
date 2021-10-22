package svenhjol.strange.module.writing_desks;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.loader.CharmModule;

import java.util.Optional;

@ClientModule(module = WritingDesks.class)
public class WritingDesksClient extends CharmModule {
    public static ItemStack tomeHolder = null;

    @Override
    public void register() {
        ScreenRegistry.register(WritingDesks.WRITING_DESK_MENU, WritingDeskScreen::new);
        ScreenRegistry.register(WritingDesks.RUNIC_LECTERN_MENU, RunicLecternScreen::new);
        BlockEntityRendererRegistry.register(WritingDesks.RUNIC_LECTERN_BLOCK_ENTITY, RunicLecternRenderer::new);
    }

    @Override
    public void runWhenEnabled() {
        ClientPlayNetworking.registerGlobalReceiver(WritingDesks.MSG_CLIENT_SET_LECTERN_TOME, this::handleSetTome);
    }

    private void handleSetTome(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender sender) {
        CompoundTag tag = buffer.readNbt();
        if (tag == null) {
            LogHelper.error(this.getClass(), "Could not read tome tag from buffer");
            return;
        }

        client.execute(() -> {
            tomeHolder = ItemStack.of(tag);
            Optional<String> runes = RunicTomeItem.getRunes(tomeHolder);
            if (runes.isEmpty()) {
                LogHelper.error(this.getClass(), "Could not fetch runes from tome");
                return;
            }
            LogHelper.debug(this.getClass(), "Tome set from server. Runes = " + runes.get());
        });
    }
}