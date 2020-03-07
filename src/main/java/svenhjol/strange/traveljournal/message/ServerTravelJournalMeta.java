package svenhjol.strange.traveljournal.message;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.traveljournal.item.TravelJournalItem;

import java.util.function.Supplier;

public class ServerTravelJournalMeta implements IMesonMessage {
    public static final int SETPAGE = 0;

    private final Hand hand;
    private final int action;
    private final int page;

    public ServerTravelJournalMeta(int action, Hand hand) {
        this.action = action;
        this.hand = hand;
        this.page = 1;
    }

    public ServerTravelJournalMeta(int action, Hand hand, int page) {
        this.action = action;
        this.hand = hand;
        this.page = page;
    }

    public static void encode(ServerTravelJournalMeta msg, PacketBuffer buf) {
        buf.writeInt(msg.action);
        buf.writeEnumValue(msg.hand);
        buf.writeInt(msg.page);
    }

    public static ServerTravelJournalMeta decode(PacketBuffer buf) {
        return new ServerTravelJournalMeta(
            buf.readInt(),
            buf.readEnumValue(Hand.class),
            buf.readInt()
        );
    }

    public static class Handler {
        public static void handle(final ServerTravelJournalMeta msg, Supplier<NetworkEvent.Context> ctx) {
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
