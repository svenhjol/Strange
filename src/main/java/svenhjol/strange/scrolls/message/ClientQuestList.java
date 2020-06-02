package svenhjol.strange.scrolls.message;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.Strange;
import svenhjol.strange.scrolls.client.QuestClient;
import svenhjol.strange.scrolls.quest.Quest;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Supplier;

public class ClientQuestList implements IMesonMessage {
    private List<IQuest> quests;

    public ClientQuestList(List<IQuest> quests) {
        this.quests = quests;
    }

    public static void encode(ClientQuestList msg, PacketBuffer buf) {
        String serialized = "";

        if (msg.quests == null) msg.quests = new ArrayList<>();
        List<String> compressed = new ArrayList<>();

        // must serialize+compress each quest nbt first
        for (IQuest quest : msg.quests) {
            try {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                CompressedStreamTools.writeCompressed(quest.toNBT(), out);
                String str = Base64.getEncoder().encodeToString(out.toByteArray());
                compressed.add(str);
            } catch (Exception e) {
                Strange.LOG.warn("Failed to compress quest: " + e.toString());
            }
        }

        // serialize the quests into a string for sending
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(out);
            so.writeObject(compressed);
            serialized = Base64.getEncoder().encodeToString(out.toByteArray());
            so.close();
        } catch (Exception e) {
            Strange.LOG.warn("Failed to output quests stream: " + e.toString());
        }

        buf.writeString(serialized);
    }

    public static ClientQuestList decode(PacketBuffer buf) {
        List<String> compressed = new ArrayList<>();
        List<IQuest> quests = new ArrayList<>();

        try {
            final byte[] byteData = Base64.getDecoder().decode(buf.readString());
            ByteArrayInputStream bi = new ByteArrayInputStream(byteData);
            ObjectInputStream si = new ObjectInputStream(bi);
            //noinspection unchecked
            compressed = (List<String>) si.readObject();
            si.close();
        } catch (Exception e) {
            Strange.LOG.warn("Failed to input quests stream: " + e.toString());
        }

        for (String s : compressed) {
            try {
                final byte[] byteData = Base64.getDecoder().decode(s);
                CompoundNBT nbt = CompressedStreamTools.readCompressed(new ByteArrayInputStream(byteData));

                Quest quest = new Quest();
                quest.fromNBT(nbt);
                quests.add(quest);
            } catch (Exception e) {
                Strange.LOG.warn("Failed to uncompress quest: " + e.toString());
            }
        }

        return new ClientQuestList(quests);
    }

    public static class Handler {
        public static void handle(final ClientQuestList msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                QuestClient.currentQuests = msg.quests;
                QuestClient.lastQuery = 0;
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
