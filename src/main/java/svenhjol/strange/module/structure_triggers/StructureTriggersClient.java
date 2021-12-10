package svenhjol.strange.module.structure_triggers;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.structure_triggers.screen.DataBlockScreen;

@ClientModule(module = StructureTriggers.class)
public class StructureTriggersClient extends CharmModule {
    @Override
    public void register() {
        ClientPlayNetworking.registerGlobalReceiver(StructureTriggers.MSG_CLIENT_OPEN_DATA_BLOCK_SCREEN, this::handleOpenDataBlockScreen);
        BlockRenderLayerMap.INSTANCE.putBlock(StructureTriggers.DATA_BLOCK, RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(StructureTriggers.IGNORE_BLOCK, RenderType.translucent());
        BlockEntityRendererRegistry.register(StructureTriggers.DATA_BLOCK_ENTITY, DataBlockEntityRenderer::new);
    }

    private void handleOpenDataBlockScreen(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        BlockPos blockPos = buffer.readBlockPos();
        client.execute(() -> {
            if (client.player == null || client.player.level == null) return;
            Player player = client.player;
            Level level = player.level;

            if (level.getBlockEntity(blockPos) instanceof DataBlockEntity data) {
                client.setScreen(new DataBlockScreen(blockPos, data));
            }
        });
    }
}
