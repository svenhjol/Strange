package svenhjol.strange.module.structure_triggers;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;

@CommonModule(mod = Strange.MOD_ID, alwaysEnabled = true)
public class StructureTriggers extends CharmModule {
    public static final ResourceLocation DATA_BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "data_block");
    public static final ResourceLocation MSG_SERVER_UPDATE_DATA_BLOCK = new ResourceLocation(Strange.MOD_ID, "server_update_data_block");
    public static final ResourceLocation MSG_CLIENT_OPEN_DATA_BLOCK_SCREEN = new ResourceLocation(Strange.MOD_ID, "client_open_data_block_screen");

    public static IgnoreBlock IGNORE_BLOCK;
    public static DataBlock DATA_BLOCK;
    public static BlockEntityType<DataBlockEntity> DATA_BLOCK_ENTITY;

    @Override
    public void register() {
        IGNORE_BLOCK = new IgnoreBlock(this);
        DATA_BLOCK = new DataBlock(this);
        DATA_BLOCK_ENTITY = CommonRegistry.blockEntity(DATA_BLOCK_ID, DataBlockEntity::new, DATA_BLOCK);

        ServerPlayNetworking.registerGlobalReceiver(MSG_SERVER_UPDATE_DATA_BLOCK, this::handleUpdateDataBlock);
    }

    private void handleUpdateDataBlock(MinecraftServer server, ServerPlayer player, ServerGamePacketListener serverGamePacketListener, FriendlyByteBuf buffer, PacketSender sender) {
        BlockPos blockPos = buffer.readBlockPos();
        CompoundTag tag = buffer.readNbt();

        server.execute(() -> {
            if (player == null || player.level == null || tag == null || tag.isEmpty()) return;
            ServerLevel level = (ServerLevel) player.level;

            if (level.getBlockEntity(blockPos) instanceof DataBlockEntity data) {
                data.load(tag);
                data.setChanged();
            }
        });
    }
}
