package svenhjol.strange.runestones.message;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.iface.IMesonMessage;

import java.util.function.Supplier;

public class ServerRunePortalAction implements IMesonMessage {
    public static final int TRAVELLED = 0;

    private final BlockPos pos;
    private final int action;

    public ServerRunePortalAction(int action, BlockPos pos) {
        this.action = action;
        this.pos = pos;
    }

    public static void encode(ServerRunePortalAction msg, PacketBuffer buf) {
        buf.writeInt(msg.action);
        buf.writeLong(msg.pos.toLong());
    }

    public static ServerRunePortalAction decode(PacketBuffer buf) {
        int action = buf.readInt();
        BlockPos pos = BlockPos.fromLong(buf.readLong());

        return new ServerRunePortalAction(action, pos);
    }

    public static class Handler {
        public static void handle(final ServerRunePortalAction msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                World world;
                ItemStack held;

                NetworkEvent.Context context = ctx.get();
                ServerPlayerEntity player = context.getSender();
                if (player == null) return;


                //noinspection SwitchStatementWithTooFewBranches
                switch (msg.action) {
                    case TRAVELLED:
                        player.invulnerableDimensionChange = false;
                        break;
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
