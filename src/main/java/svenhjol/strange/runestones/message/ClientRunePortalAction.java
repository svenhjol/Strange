package svenhjol.strange.runestones.message;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.runestones.module.RunePortals;

import java.util.function.Supplier;

public class ClientRunePortalAction implements IMesonMessage {
    public static final int TRAVELLED = 0;

    private final BlockPos pos;
    private final int action;

    public ClientRunePortalAction(int action, BlockPos pos) {
        this.action = action;
        this.pos = pos;
    }

    public static void encode(ClientRunePortalAction msg, PacketBuffer buf) {
        buf.writeInt(msg.action);
        buf.writeLong(msg.pos.toLong());
    }

    public static ClientRunePortalAction decode(PacketBuffer buf) {
        int action = buf.readInt();
        BlockPos pos = BlockPos.fromLong(buf.readLong());

        return new ClientRunePortalAction(action, pos);
    }

    public static class Handler {
        public static void handle(final ClientRunePortalAction msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                //noinspection SwitchStatementWithTooFewBranches
                switch (msg.action) {
                    case TRAVELLED:
                        RunePortals.effectPortalTravelled(msg.pos);
                        break;
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
