package svenhjol.strange.base.message;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.List;
import java.util.function.Supplier;

public class RequestCurrentQuests implements IMesonMessage
{
    private boolean test;

    public RequestCurrentQuests()
    {
        // no op
    }

    public static void encode(RequestCurrentQuests msg, PacketBuffer buf)
    {
        // no op
    }

    public static RequestCurrentQuests decode(PacketBuffer buf)
    {
        return new RequestCurrentQuests();
    }

    public static class Handler
    {
        public static void handle(final RequestCurrentQuests msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                NetworkEvent.Context context = ctx.get();
                ServerPlayerEntity player = context.getSender();
                if (player == null) return;

                List<IQuest> currentQuests = Quests.getCapability(player).getCurrentQuests(player);
                PacketHandler.sendTo(new SendCurrentQuests(currentQuests), player);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
