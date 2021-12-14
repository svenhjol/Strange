package svenhjol.strange.module.structures;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;

@CommonModule(mod = Strange.MOD_ID, alwaysEnabled = true, priority = 10)
public class Structures extends CharmModule {
    public static final ResourceLocation DATA_BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "data_block");
    public static final ResourceLocation ENTITY_BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "entity_block");

    public static final ResourceLocation MSG_SERVER_UPDATE_BLOCK_ENTITY = new ResourceLocation(Strange.MOD_ID, "server_update_data_block");
    public static final ResourceLocation MSG_CLIENT_OPEN_DATA_BLOCK_SCREEN = new ResourceLocation(Strange.MOD_ID, "client_open_data_block_screen");
    public static final ResourceLocation MSG_CLIENT_OPEN_ENTITY_BLOCK_SCREEN = new ResourceLocation(Strange.MOD_ID, "client_open_entity_block_screen");

    public static IgnoreBlock IGNORE_BLOCK;
    public static DataBlock DATA_BLOCK;
    public static EntityBlock ENTITY_BLOCK;
    public static BlockEntityType<DataBlockEntity> DATA_BLOCK_ENTITY;
    public static BlockEntityType<EntityBlockEntity> ENTITY_BLOCK_ENTITY;

    public static int entityTriggerDistance = 16;

    @Override
    public void register() {
        IGNORE_BLOCK = new IgnoreBlock(this);
        ENTITY_BLOCK = new EntityBlock(this);
        DATA_BLOCK = new DataBlock(this);
        ENTITY_BLOCK_ENTITY = CommonRegistry.blockEntity(ENTITY_BLOCK_ID, EntityBlockEntity::new, ENTITY_BLOCK);
        DATA_BLOCK_ENTITY = CommonRegistry.blockEntity(DATA_BLOCK_ID, DataBlockEntity::new, DATA_BLOCK);

        Processors.init();

        ServerPlayNetworking.registerGlobalReceiver(MSG_SERVER_UPDATE_BLOCK_ENTITY, this::handleUpdateBlockEntity);
    }

    private void handleUpdateBlockEntity(MinecraftServer server, ServerPlayer player, ServerGamePacketListener serverGamePacketListener, FriendlyByteBuf buffer, PacketSender sender) {
        BlockPos pos = buffer.readBlockPos();
        CompoundTag tag = buffer.readNbt();

        server.execute(() -> {
            if (player == null || player.level == null || tag == null || tag.isEmpty()) return;
            ServerLevel level = (ServerLevel) player.level;

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                blockEntity.load(tag);
                blockEntity.setChanged();
            }
        });
    }
}
