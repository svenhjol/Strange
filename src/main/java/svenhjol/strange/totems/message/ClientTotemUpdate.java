package svenhjol.strange.totems.message;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.base.TotemHelper;

import java.util.function.Supplier;

public class ClientTotemUpdate implements IMesonMessage
{
    public static int DAMAGE = 0;
    public static int DESTROY = 1;

    private int status;
    private BlockPos pos;

    public ClientTotemUpdate(int status, BlockPos pos)
    {
        this.status = status;
        this.pos = pos;
    }

    public static void encode(ClientTotemUpdate msg, PacketBuffer buf)
    {
        buf.writeInt(msg.status);
        buf.writeLong(msg.pos.toLong());
    }

    public static ClientTotemUpdate decode(PacketBuffer buf)
    {
        return new ClientTotemUpdate(buf.readInt(), BlockPos.fromLong(buf.readLong()));
    }

    public static class Handler
    {
        public static void handle(final ClientTotemUpdate msg, Supplier <NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                if (msg.status == DAMAGE) {
                    TotemHelper.effectDamageTotem(msg.pos);
                } else if (msg.status == DESTROY) {
                    TotemHelper.effectDestroyTotem(msg.pos);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
