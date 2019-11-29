package svenhjol.strange.totems.message;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.totems.module.TotemOfFlying;

import java.util.function.Supplier;

public class ClientTotemUpdateFlying implements IMesonMessage
{
    public static int DISABLE = 0;
    public static int ENABLE = 1;

    private int status;

    public ClientTotemUpdateFlying(int status)
    {
        this.status = status;
    }

    public static void encode(ClientTotemUpdateFlying msg, PacketBuffer buf)
    {
        buf.writeInt(msg.status);
    }

    public static ClientTotemUpdateFlying decode(PacketBuffer buf)
    {
        return new ClientTotemUpdateFlying(buf.readInt());
    }

    public static class Handler
    {
        public static void handle(final ClientTotemUpdateFlying msg, Supplier <NetworkEvent.Context> ctx)
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
