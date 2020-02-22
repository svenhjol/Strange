package svenhjol.strange.base.message;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.base.StrangeLoader;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.function.Supplier;

public class ClientUpdatePlayerState implements IMesonMessage
{
    private CompoundNBT structures;

    public ClientUpdatePlayerState(CompoundNBT structures)
    {
        this.structures = structures;
    }

    public static void encode(ClientUpdatePlayerState msg, PacketBuffer buf)
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

    public static ClientUpdatePlayerState decode(PacketBuffer buf)
    {
        CompoundNBT structures = new CompoundNBT();

        try {
            final byte[] byteData = DatatypeConverter.parseBase64Binary(buf.readString());
            structures = CompressedStreamTools.readCompressed(new ByteArrayInputStream(byteData));
        } catch (Exception e) {
            Meson.warn("Failed to uncompress structures");
        }

        return new ClientUpdatePlayerState(structures);
    }

    public static class Handler
    {
        public static void handle(final ClientUpdatePlayerState msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
//                Meson.debug("UpdatePlayerState heartbeat response");
                StrangeLoader.client.updateStructures(msg.structures);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
