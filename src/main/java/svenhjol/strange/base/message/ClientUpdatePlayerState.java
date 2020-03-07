package svenhjol.strange.base.message;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.Strange;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.function.Supplier;

public class ClientUpdatePlayerState implements IMesonMessage {
    private final CompoundNBT input;

    public ClientUpdatePlayerState(CompoundNBT input) {
        this.input = input;
    }

    public static void encode(ClientUpdatePlayerState msg, PacketBuffer buf) {
        String serialized = "";

        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(msg.input, out);
            serialized = DatatypeConverter.printBase64Binary(out.toByteArray());
        } catch (Exception e) {
            Strange.LOG.warn("Failed to compress structures");
        }
        buf.writeString(serialized);
    }

    public static ClientUpdatePlayerState decode(PacketBuffer buf) {
        CompoundNBT input = new CompoundNBT();

        try {
            final byte[] byteData = DatatypeConverter.parseBase64Binary(buf.readString());
            input = CompressedStreamTools.readCompressed(new ByteArrayInputStream(byteData));
        } catch (Exception e) {
            Strange.LOG.warn("Failed to uncompress structures");
        }

        return new ClientUpdatePlayerState(input);
    }

    public static class Handler {
        public static void handle(final ClientUpdatePlayerState msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                Strange.client.updateStructures(msg.input);
                Strange.client.updateDiscoveries(msg.input);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
