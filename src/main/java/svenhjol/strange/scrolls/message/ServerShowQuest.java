package svenhjol.strange.scrolls.message;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.scrolls.module.Quests;

import java.util.function.Supplier;

public class ServerShowQuest implements IMesonMessage
{
    private String id;

    public ServerShowQuest(String id)
    {
        this.id = id;
    }

    public static void encode(ServerShowQuest msg, PacketBuffer buf)
    {
        buf.writeString(msg.id);
    }

    public static ServerShowQuest decode(PacketBuffer buf)
    {
        return new ServerShowQuest(buf.readString());
    }

    public static class Handler
    {
        public static void handle(final ServerShowQuest msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                NetworkEvent.Context context = ctx.get();
                ServerPlayerEntity player = context.getSender();
                if (player == null) return;

                Quests.getCurrentQuestById(player, msg.id)
                    .ifPresent(q -> Quests.proxy.showQuestScreen(player, q));
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
