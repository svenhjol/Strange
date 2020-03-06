package svenhjol.strange.scrolls.message;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.scrolls.event.QuestEvent;
import svenhjol.strange.scrolls.Quests;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Server interprets actions taken on a quest by the client.
 */
public class ServerQuestAction implements IMesonMessage
{
    public static final int QUIT = 0;

    private int action;
    private String id;

    public ServerQuestAction(int action, String id)
    {
        this.id = id;
        this.action = action;
    }

    public static void encode(ServerQuestAction msg, PacketBuffer buf)
    {
        buf.writeInt(msg.action);
        buf.writeString(msg.id);
    }

    public static ServerQuestAction decode(PacketBuffer buf)
    {
        return new ServerQuestAction(
            buf.readInt(),
            buf.readString(32)
        );
    }

    public static class Handler
    {
        public static void handle(final ServerQuestAction msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                NetworkEvent.Context context = ctx.get();
                ServerPlayerEntity player = context.getSender();
                if (player == null) return;

                Optional<IQuest> quest = Quests.getCurrentQuestById(player, msg.id);

                switch (msg.action) {
                    case QUIT:
                        quest.ifPresent(iQuest -> MinecraftForge.EVENT_BUS.post(new QuestEvent.Decline(player, iQuest)));
                        Quests.getCapability(player).updateCurrentQuests(player);
                        break;
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
