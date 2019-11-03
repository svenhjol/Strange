package svenhjol.strange.scrolls.message;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.List;
import java.util.function.Supplier;

public class ServerCurrentQuests implements IMesonMessage
{
    private boolean test;

    public ServerCurrentQuests()
    {
        // no op
    }

    public static void encode(ServerCurrentQuests msg, PacketBuffer buf)
    {
        // no op
    }

    public static ServerCurrentQuests decode(PacketBuffer buf)
    {
        return new ServerCurrentQuests();
    }

    public static class Handler
    {
        public static void handle(final ServerCurrentQuests msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                NetworkEvent.Context context = ctx.get();
                ServerPlayerEntity player = context.getSender();
                if (player == null) return;

                List<IQuest> currentQuests = Quests.getCapability(player).getCurrentQuests(player);
                PacketHandler.sendTo(new ClientCurrentQuests(currentQuests), player);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
