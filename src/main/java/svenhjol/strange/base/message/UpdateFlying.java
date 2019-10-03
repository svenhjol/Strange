package svenhjol.strange.base.message;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.totems.module.TotemOfFlying;

import java.util.function.Supplier;

public class UpdateFlying implements IMesonMessage
{
    public static int DISABLE = 0;
    public static int ENABLE = 1;

    private int status;
    private BlockPos pos;

    public UpdateFlying(int status)
    {
        this.status = status;
    }

    public static void encode(UpdateFlying msg, PacketBuffer buf)
    {
        buf.writeInt(msg.status);
    }

    public static UpdateFlying decode(PacketBuffer buf)
    {
        return new UpdateFlying(buf.readInt());
    }

    public static class Handler
    {
        public static void handle(final UpdateFlying msg, Supplier <NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                if (msg.status == ENABLE) {
                    TotemOfFlying.enableFlight();
                } else if (msg.status == DISABLE) {
                    TotemOfFlying.disableFlight();
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

}
