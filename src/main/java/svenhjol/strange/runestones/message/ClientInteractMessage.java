package svenhjol.strange.runestones.message;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.runestones.module.Runestones;

import java.util.function.Supplier;

public class ClientInteractMessage implements IMesonMessage
{
    public static final int ACTIVATE = 0;
    public static final int TRAVELLED = 1;
    public static final int DISCOVERED = 2;

    private int action;
    private BlockPos pos;

    public ClientInteractMessage(int action, BlockPos pos)
    {
        this.action = action;
        this.pos = pos;
    }

    public static void encode(ClientInteractMessage msg, PacketBuffer buf)
    {
        buf.writeInt(msg.action);
        buf.writeLong(msg.pos.toLong());
    }

    public static ClientInteractMessage decode(PacketBuffer buf)
    {
        return new ClientInteractMessage(buf.readInt(), BlockPos.fromLong(buf.readLong()));
    }

    public static class Handler
    {
        public static void handle(final ClientInteractMessage msg, Supplier <NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                if (msg.action == ACTIVATE) {
                    Runestones.effectActivate(msg.pos);
                }
                if (msg.action == TRAVELLED) {
                    Runestones.effectTravelled(msg.pos);
                }
                if (msg.action == DISCOVERED) {
                    Runestones.effectDiscovered(msg.pos);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
