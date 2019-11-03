package svenhjol.strange.scrolls.message;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.scrolls.client.QuestClient;
import svenhjol.strange.scrolls.quest.Quest;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClientCurrentQuests implements IMesonMessage
{
    private String serialized = "";
    private List<IQuest> quests;

    public ClientCurrentQuests(List<IQuest> quests)
    {
        this.quests = quests;
        List<String> compressed = new ArrayList<>();

        // must serialize+compress each quest nbt first
        for (IQuest quest : quests) {
            try {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                CompressedStreamTools.writeCompressed( quest.toNBT(), out );
                String str = DatatypeConverter.printBase64Binary( out.toByteArray() );
                compressed.add(str);
            } catch (Exception e) {
                Meson.log("Failed to compress quest", e);
            }
        }

        // serialize the quests into a string for sending
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(out);
            so.writeObject(compressed);
            serialized = DatatypeConverter.printBase64Binary( out.toByteArray() );
            so.close();
        } catch (Exception e) {
            Meson.log("Failed to output quests stream", e);
        }
    }

    public static void encode(ClientCurrentQuests msg, PacketBuffer buf)
    {
        buf.writeString(msg.serialized);
    }

    public static ClientCurrentQuests decode(PacketBuffer buf)
    {
        List<String> compressed = new ArrayList<>();
        List<IQuest> quests = new ArrayList<>();

        try {
            final byte[] byteData = DatatypeConverter.parseBase64Binary( buf.readString() );
            ByteArrayInputStream bi = new ByteArrayInputStream(byteData);
            ObjectInputStream si = new ObjectInputStream(bi);
            compressed = (List<String>)si.readObject();
            si.close();
        } catch (Exception e) {
            Meson.log("Failed to input quests stream", e);
        }

        for (String s : compressed) {
            try {
                final byte[] byteData = DatatypeConverter.parseBase64Binary(s);
                CompoundNBT nbt = CompressedStreamTools.readCompressed( new ByteArrayInputStream( byteData ) );

                Quest quest = new Quest();
                quest.fromNBT(nbt);
                quests.add(quest);
            } catch (Exception e) {
                Meson.log("Failed to uncompress quest", e);
            }
        }

        return new ClientCurrentQuests(quests);
    }

    public static class Handler
    {
        public static void handle(final ClientCurrentQuests msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                QuestClient.currentQuests = msg.quests;
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
