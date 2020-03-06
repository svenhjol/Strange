package svenhjol.strange.scrolls.message;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.scrolls.Quests;

import java.util.function.Supplier;

public class ClientScrollAction implements IMesonMessage
{
    private String id;
    private Hand hand;

    public ClientScrollAction(String id, Hand hand)
    {
        this.id = id;
        this.hand = hand;
    }

    public static void encode(ClientScrollAction msg, PacketBuffer buf)
    {
        buf.writeString(msg.id);
        buf.writeEnumValue(msg.hand);
    }

    public static ClientScrollAction decode(PacketBuffer buf)
    {
        String id = buf.readString();
        Hand hand = buf.readEnumValue(Hand.class);

        return new ClientScrollAction(id, hand);
    }

    public static class Handler
    {
        public static void handle(final ClientScrollAction msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                Quests.client.showScroll(msg.hand);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
