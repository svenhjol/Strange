package svenhjol.strange.traveljournal.message;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.Strange;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.function.Supplier;

public class ClientTravelJournalEntries implements IMesonMessage {
    private final CompoundNBT entries;

    public ClientTravelJournalEntries(CompoundNBT entries) {
        this.entries = entries;
    }

    public static void encode(ClientTravelJournalEntries msg, PacketBuffer buf) {
        String serialized = "";

        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(msg.entries, out);
            serialized = Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            Strange.LOG.warn("Failed to compress entries");
        }

        buf.writeString(serialized);
    }

    public static ClientTravelJournalEntries decode(PacketBuffer buf) {
        CompoundNBT entries = new CompoundNBT();

        try {
            final byte[] byteData = Base64.getDecoder().decode(buf.readString());
            entries = CompressedStreamTools.readCompressed(new ByteArrayInputStream(byteData));
        } catch (Exception e) {
            Strange.LOG.warn("Failed to uncompress entries");
        }

        return new ClientTravelJournalEntries(entries);
    }

    public static class Handler {
        public static CompoundNBT entries = new CompoundNBT();
        public static boolean updated = false;

        public static void handle(final ClientTravelJournalEntries msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                entries = msg.entries;
                updated = true;
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
