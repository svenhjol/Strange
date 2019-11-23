package svenhjol.strange.magic.message;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.magic.item.StaffItem;

import java.util.function.Supplier;

/**
 * Server interprets actions taken on a quest by the client.
 */
public class ServerStaffAction implements IMesonMessage
{
    public static final int CAST = 0;

    private int action;
    private Hand hand;

    public ServerStaffAction(int action, Hand hand)
    {
        this.action = action;
        this.hand = hand;
    }

    public static void encode(ServerStaffAction msg, PacketBuffer buf)
    {
        buf.writeInt(msg.action);
        buf.writeEnumValue(msg.hand);
    }

    public static ServerStaffAction decode(PacketBuffer buf)
    {
        return new ServerStaffAction(
            buf.readInt(),
            buf.readEnumValue(Hand.class)
        );
    }

    public static class Handler
    {
        public static void handle(final ServerStaffAction msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                NetworkEvent.Context context = ctx.get();
                ServerPlayerEntity player = context.getSender();
                if (player == null) return;

                ItemStack staff = player.getHeldItem(msg.hand);
                if (!(staff.getItem() instanceof StaffItem)) return;

                switch (msg.action) {
                    case CAST:
                        if (StaffItem.getNumberOfSpells(staff) > 0) {
                            boolean result = StaffItem.cast(player, staff);
                        }
                        break;

                    default:
                        break;
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}

