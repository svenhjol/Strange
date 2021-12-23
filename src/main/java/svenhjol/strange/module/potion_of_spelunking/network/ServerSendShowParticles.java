package svenhjol.strange.module.potion_of_spelunking.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;

import java.util.Map;

@Id("strange:show_spelunking_particles")
public class ServerSendShowParticles extends ServerSender {
    public void send(ServerPlayer player, Map<BlockPos, DyeColor> map) {
        super.send(player, buf -> {
            // ore positions (blockpos to longs)
            buf.writeLongArray(map.keySet()
                .stream()
                .map(BlockPos::asLong)
                .mapToLong(Long::longValue).toArray());

            // ore colors (dyecolor to ints)
            buf.writeVarIntArray(map.values()
                .stream()
                .map(DyeColor::getId)
                .mapToInt(Integer::intValue).toArray());
        });
    }

    @Override
    protected boolean showDebugMessages() {
        return false;
    }
}
