package svenhjol.strange.base.message;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.totems.module.TotemOfFlying;

import java.util.function.Supplier;

public class DamageTotemOfFlying implements IMesonMessage
{
    private BlockPos pos;

    public DamageTotemOfFlying(BlockPos pos)
    {
        this.pos = pos;
    }

    public static void encode(DamageTotemOfFlying msg, PacketBuffer buf)
    {
        buf.writeLong(msg.pos.toLong());
    }

    public static DamageTotemOfFlying decode(PacketBuffer buf)
    {
        return new DamageTotemOfFlying(BlockPos.fromLong(buf.readLong()));
    }

    public static class Handler
    {
        public static void handle(final DamageTotemOfFlying msg, Supplier <NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> TotemOfFlying.effectDamageTotem(msg.pos));
            ctx.get().setPacketHandled(true);
        }
    }
}
