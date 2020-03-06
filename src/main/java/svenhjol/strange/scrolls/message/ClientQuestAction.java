package svenhjol.strange.scrolls.message;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.Strange;
import svenhjol.strange.scrolls.client.toast.QuestToastTypes.Type;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.Quest;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.function.Supplier;

public class ClientQuestAction implements IMesonMessage
{
    public static final int SHOW = 0;
    public static final int ACCEPTED = 1;
    public static final int COMPLETED = 2;
    public static final int DECLINED = 3;
    public static final int FAILED = 4;

    private IQuest quest;
    private int action;

    public ClientQuestAction(int action, IQuest quest)
    {
        this.action = action;
        this.quest = quest;
    }

    public static void encode(ClientQuestAction msg, PacketBuffer buf)
    {
        String serialized = "";

        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(msg.quest.toNBT(), out);
            serialized = DatatypeConverter.printBase64Binary(out.toByteArray());
        } catch (Exception e) {
            Meson.warn("Failed to compress quest");
        }

        buf.writeInt(msg.action);
        buf.writeString(serialized);
    }

    public static ClientQuestAction decode(PacketBuffer buf)
    {
        IQuest quest = new Quest();

        int action = buf.readInt();
        try {
            final byte[] byteData = DatatypeConverter.parseBase64Binary(buf.readString());
            quest.fromNBT(CompressedStreamTools.readCompressed(new ByteArrayInputStream(byteData)));
        } catch (Exception e) {
            Meson.warn("Failed to uncompress quest");
        }

        return new ClientQuestAction(action, quest);
    }

    public static class Handler
    {
        public static void handle(final ClientQuestAction msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {

                Meson.getInstance(Strange.MOD_ID).getPacketHandler().sendToServer(new ServerQuestList());

                switch (msg.action) {
                    case SHOW:
                        Quests.client.showQuest(msg.quest);
                        break;

                    case ACCEPTED:
                        Quests.client.toast(msg.quest, Type.General, "event.strange.quests.accepted");
                        break;

                    case COMPLETED:
                        Quests.client.toast(msg.quest, Type.Success, "event.strange.quests.completed");
                        break;

                    case DECLINED:
                        Quests.client.toast(msg.quest, Type.General, "event.strange.quests.declined");
                        break;

                    case FAILED:
                        Quests.client.toast(msg.quest, Type.Failed, "event.strange.quests.failed");
                        break;
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
