package svenhjol.strange.ambience.message;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.ambience.client.AmbienceHandler;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.function.Supplier;

public class ClientUpdateStructures implements IMesonMessage
{
    private CompoundNBT structures;

    public ClientUpdateStructures(CompoundNBT structures)
    {
        this.structures = structures;
    }

    public static void encode(ClientUpdateStructures msg, PacketBuffer buf)
    {
        String serialized = "";

        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(msg.structures, out);
            serialized = DatatypeConverter.printBase64Binary(out.toByteArray());
        } catch (Exception e) {
            Meson.warn("Failed to compress structures");
        }
        buf.writeString(serialized);
    }

    public static ClientUpdateStructures decode(PacketBuffer buf)
    {
        CompoundNBT structures = new CompoundNBT();

        try {
            final byte[] byteData = DatatypeConverter.parseBase64Binary(buf.readString());
            structures = CompressedStreamTools.readCompressed(new ByteArrayInputStream(byteData));
        } catch (Exception e) {
            Meson.warn("Failed to uncompress structures");
        }

        return new ClientUpdateStructures(structures);
    }

    public static class Handler
    {
        public static void handle(final ClientUpdateStructures msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                AmbienceHandler.updateStructures(msg.structures);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
