package svenhjol.strange.traveljournal.message;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.traveljournal.client.screen.TravelJournalScreen;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.function.Supplier;

public class ClientEntriesMessage implements IMesonMessage
{
    private String serialized = "";
    private CompoundNBT entries;

    public ClientEntriesMessage(CompoundNBT entries)
    {
        this.entries = entries;

        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(entries, out);
            serialized = DatatypeConverter.printBase64Binary(out.toByteArray());
        } catch (Exception e) {
            Meson.warn("Failed to compress entries");
        }
    }

    public static void encode(ClientEntriesMessage msg, PacketBuffer buf)
    {
        buf.writeString(msg.serialized);
    }

    public static ClientEntriesMessage decode(PacketBuffer buf)
    {
        CompoundNBT entries = new CompoundNBT();

        try {
            final byte[] byteData = DatatypeConverter.parseBase64Binary(buf.readString());
            entries = CompressedStreamTools.readCompressed(new ByteArrayInputStream(byteData));
        } catch (Exception e) {
            Meson.warn("Failed to uncompress entries");
        }

        return new ClientEntriesMessage(entries);
    }

    public static class Handler
    {
        public static CompoundNBT entries = new CompoundNBT();
        public static boolean updated = false;

        public static void handle(final ClientEntriesMessage msg, Supplier <NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                entries = msg.entries;
                updated = true;
            });
            ctx.get().setPacketHandled(true);
        }

        public static void clearUpdates()
        {
            entries = new CompoundNBT();
            updated = false;
        }

        @OnlyIn(Dist.CLIENT)
        private static void openTravelJournal(PlayerEntity player, Hand hand)
        {
            Minecraft.getInstance().displayGuiScreen(new TravelJournalScreen(player, hand));
        }

        @OnlyIn(Dist.CLIENT)
        private static void openScreenshotEntry(PlayerEntity player, Hand hand, String id)
        {
            Minecraft mc = Minecraft.getInstance();
        }

        @OnlyIn(Dist.CLIENT)
        private static void openUpdateEntry(PlayerEntity player, Hand hand, String id)
        {
            Minecraft mc = Minecraft.getInstance();
        }
    }
}
