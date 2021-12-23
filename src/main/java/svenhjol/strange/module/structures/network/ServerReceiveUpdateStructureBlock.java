package svenhjol.strange.module.structures.network;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerReceiver;

@Id("strange:update_structure_block")
public class ServerReceiveUpdateStructureBlock extends ServerReceiver {
    @Override
    public void handle(MinecraftServer server, ServerPlayer player, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        CompoundTag tag = getCompoundTag(buffer).orElseThrow();
        if (player == null || player.level == null || tag.isEmpty()) return;
        ServerLevel level = (ServerLevel) player.level;

        server.execute(() -> {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                blockEntity.load(tag);
                blockEntity.setChanged();
            }
        });
    }
}
