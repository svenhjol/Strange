package svenhjol.strange.runicaltars;

import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RunicAltarsServer {
    public void init() {
        ServerSidePacketRegistry.INSTANCE.register(RunicAltars.MSG_SERVER_PLACED_ON_ALTAR, this::handleServerPlacedOnAltar);
    }

    private void handleServerPlacedOnAltar(PacketContext context, PacketByteBuf data) {
        BlockPos pos = BlockPos.fromLong(data.readLong());
        context.getTaskQueue().execute(() -> {
            ServerPlayerEntity player = (ServerPlayerEntity)context.getPlayer();
            if (player == null) return;

            World world = player.world;
            RunicAltarBlockEntity blockEntity = (RunicAltarBlockEntity)world.getBlockEntity(pos);
            if (blockEntity == null)
                return;

            ItemStack stack = blockEntity.getStack(0);
            Criteria.CONSUME_ITEM.trigger(player, stack);
        });
    }
}
