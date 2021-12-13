package svenhjol.strange.module.structures;

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
import svenhjol.strange.module.structures.screen.DataBlockScreen;
import svenhjol.strange.module.structures.screen.EntityBlockScreen;

@ClientModule(module = Structures.class)
public class StructuresClient extends CharmModule {
    @Override
    public void register() {
        ClientPlayNetworking.registerGlobalReceiver(Structures.MSG_CLIENT_OPEN_DATA_BLOCK_SCREEN, this::handleOpenDataBlockScreen);
        ClientPlayNetworking.registerGlobalReceiver(Structures.MSG_CLIENT_OPEN_ENTITY_BLOCK_SCREEN, this::handleOpenEntityBlockScreen);

        BlockRenderLayerMap.INSTANCE.putBlock(Structures.DATA_BLOCK, RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Structures.ENTITY_BLOCK, RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Structures.IGNORE_BLOCK, RenderType.translucent());

        BlockEntityRendererRegistry.register(Structures.DATA_BLOCK_ENTITY, DataBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(Structures.ENTITY_BLOCK_ENTITY, EntityBlockEntityRenderer::new);
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

    private void handleOpenEntityBlockScreen(Minecraft client, ClientPacketListener listener, FriendlyByteBuf buffer, PacketSender sender) {
        BlockPos blockPos = buffer.readBlockPos();
        client.execute(() -> {
            if (client.player == null || client.player.level == null) return;
            Player player = client.player;
            Level level = player.level;

            if (level.getBlockEntity(blockPos) instanceof EntityBlockEntity blockEntity) {
                client.setScreen(new EntityBlockScreen(blockPos, blockEntity));
            }
        });
    }
}
