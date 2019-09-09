package svenhjol.strange.base.message;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.scrolls.module.Quests;

import java.util.function.Supplier;

public class RequestShowQuest implements IMesonMessage
{
    private String id;

    public RequestShowQuest(String id)
    {
        this.id = id;
    }

    public static void encode(RequestShowQuest msg, PacketBuffer buf)
    {
        buf.writeString(msg.id);
    }

    public static RequestShowQuest decode(PacketBuffer buf)
    {
        return new RequestShowQuest(buf.readString());
    }

    public static class Handler
    {
        public static void handle(final RequestShowQuest msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                NetworkEvent.Context context = ctx.get();
                ServerPlayerEntity player = context.getSender();
                if (player == null) return;

                Quests.getCurrentQuestById(player, msg.id)
                    .ifPresent(q -> Quests.showQuestScreen(player, q));
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
