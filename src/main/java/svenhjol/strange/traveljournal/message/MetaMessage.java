package svenhjol.strange.traveljournal.message;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.traveljournal.item.TravelJournalItem;

import java.util.function.Supplier;

public class MetaMessage implements IMesonMessage
{
    public static final int SETPAGE = 0;

    private Hand hand;
    private int action;
    private int page;

    public MetaMessage(int action, Hand hand)
    {
        this.action = action;
        this.hand = hand;
        this.page = 1;
    }

    public MetaMessage(int action, Hand hand, int page)
    {
        this.action = action;
        this.hand = hand;
        this.page = page;
    }

    public static void encode(MetaMessage msg, PacketBuffer buf)
    {
        buf.writeInt(msg.action);
        buf.writeEnumValue(msg.hand);
        buf.writeInt(msg.page);
    }

    public static MetaMessage decode(PacketBuffer buf)
    {
        return new MetaMessage(
            buf.readInt(),
            buf.readEnumValue(Hand.class),
            buf.readInt()
        );
    }

    public static class Handler
    {
        public static void handle(final MetaMessage msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                NetworkEvent.Context context = ctx.get();
                ServerPlayerEntity player = context.getSender();
                if (player == null) return;

                ItemStack held = player.getHeldItem(msg.hand);

                if (msg.action == SETPAGE) {
                    TravelJournalItem.setPage(held, msg.page);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
