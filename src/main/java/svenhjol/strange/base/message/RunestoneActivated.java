package svenhjol.strange.base.message;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.runestones.module.Runestones;

import java.util.function.Supplier;

public class RunestoneActivated implements IMesonMessage
{
    private BlockPos pos;

    public RunestoneActivated(BlockPos pos)
    {
        this.pos = pos;
    }

    public static void encode(RunestoneActivated msg, PacketBuffer buf)
    {
        buf.writeLong(msg.pos.toLong());
    }

    public static RunestoneActivated decode(PacketBuffer buf)
    {
        return new RunestoneActivated(BlockPos.fromLong(buf.readLong()));
    }

    public static class Handler
    {
        public static void handle(final RunestoneActivated msg, Supplier <NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> Runestones.effectActivate(msg.pos));
            ctx.get().setPacketHandled(true);
        }
    }
}
