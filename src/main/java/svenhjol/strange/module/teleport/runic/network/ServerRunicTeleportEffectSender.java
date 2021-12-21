package svenhjol.strange.module.teleport.runic.network;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import svenhjol.strange.Strange;
import svenhjol.strange.module.teleport.runic.RunicTeleport;
import svenhjol.strange.network.ServerSend;

public class ServerRunicTeleportEffectSender extends ServerSend {
    @Override
    public ResourceLocation id() {
        return new ResourceLocation(Strange.MOD_ID, "runic_teleport_effect");
    }

    public void send(ServerPlayer player, BlockPos origin, RunicTeleport.Type type) {
        send(player, buf -> {
            buf.writeEnum(type);
            buf.writeBlockPos(origin);
        });
    }
}
