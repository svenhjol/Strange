package svenhjol.strange.traveljournal.message;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.traveljournal.Entry;
import svenhjol.strange.traveljournal.module.TravelJournal;

import java.util.function.Supplier;

public class ClientTravelJournalAction implements IMesonMessage {
    public static final int SCREENSHOT = 0;
    public static final int ADD = 1;

    public int action;
    public String id;
    public String name;
    public BlockPos pos;
    public int dim;
    public int color;
    public Hand hand;

    public ClientTravelJournalAction(int action, Entry entry, Hand hand) {
        this(action, entry.id, entry.name, entry.pos, entry.dim, entry.color, hand);
    }

    public ClientTravelJournalAction(int action, String id, String name, BlockPos pos, int dim, int color, Hand hand) {
        this.action = action;
        this.id = id;
        this.name = name;
        this.pos = pos;
        this.dim = dim;
        this.color = color;
        this.hand = hand;
    }

    public static void encode(ClientTravelJournalAction msg, PacketBuffer buf) {
        long pos = msg.pos == null ? 0 : msg.pos.toLong();

        buf.writeInt(msg.action);
        buf.writeString(msg.id);
        buf.writeString(msg.name);
        buf.writeLong(pos);
        buf.writeInt(msg.dim);
        buf.writeInt(msg.color);
        buf.writeEnumValue(msg.hand);
    }

    public static ClientTravelJournalAction decode(PacketBuffer buf) {
        int action = buf.readInt();
        String id = buf.readString();
        String title = buf.readString();
        BlockPos pos = BlockPos.fromLong(buf.readLong());
        int dim = buf.readInt();
        int color = buf.readInt();
        Hand hand = buf.readEnumValue(Hand.class);
        return new ClientTravelJournalAction(action, id, title, pos, dim, color, hand);
    }

    public static class Handler {
        public static void handle(final ClientTravelJournalAction msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                Entry entry = new Entry(msg.id, msg.name, msg.pos, msg.dim, msg.color);

                if (msg.action == ADD) {
                    TravelJournal.client.handleAddEntry(entry, msg.hand);
                }
                if (msg.action == SCREENSHOT) {
                    TravelJournal.client.handleScreenshot(entry, msg.hand);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
