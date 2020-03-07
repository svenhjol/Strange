package svenhjol.strange.scrolls.message;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.Strange;
import svenhjol.strange.scrolls.event.QuestEvent;
import svenhjol.strange.scrolls.item.ScrollItem;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.function.Supplier;

/**
 * Server interprets actions taken on a quest by the client.
 */
public class ServerScrollAction implements IMesonMessage {
    public static final int SHOW = 0;
    public static final int ACCEPT = 1;
    public static final int DECLINE = 2;

    private int action;
    private String id;
    private Hand hand;

    public ServerScrollAction(int action, String id, Hand hand) {
        this.id = id;
        this.action = action;
        this.hand = hand;
    }

    public static void encode(ServerScrollAction msg, PacketBuffer buf) {
        buf.writeInt(msg.action);
        buf.writeString(msg.id);
        buf.writeEnumValue(msg.hand);
    }

    public static ServerScrollAction decode(PacketBuffer buf) {
        return new ServerScrollAction(
            buf.readInt(),
            buf.readString(32),
            buf.readEnumValue(Hand.class)
        );
    }

    public static class Handler {
        public static void handle(final ServerScrollAction msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                NetworkEvent.Context context = ctx.get();
                ServerPlayerEntity player = context.getSender();
                if (player == null) return;

                IQuest quest;
                ItemStack held = player.getHeldItem(msg.hand);

                switch (msg.action) {
                    case SHOW:
                        Quests.getCurrentQuestById(player, msg.id)
                            .ifPresent(q -> Meson.getInstance(Strange.MOD_ID).getPacketHandler().sendTo(new ClientScrollAction(msg.id, msg.hand), player));
                        break;

                    case ACCEPT:
                        if (Quests.getCurrent(player).size() < Quests.getMaxQuests()) {
                            quest = ScrollItem.getQuest(held);
                            if (!MinecraftForge.EVENT_BUS.post(new QuestEvent.Accept(player, quest)))
                                shrinkStack(held);
                        } else {
                            player.sendMessage(new TranslationTextComponent("gui.strange.quests.too_many_quests"));
                        }
                        break;

                    case DECLINE:
                        quest = ScrollItem.getQuest(held);
                        shrinkStack(held);
                        MinecraftForge.EVENT_BUS.post(new QuestEvent.Decline(player, quest));
                        break;
                }
            });
            ctx.get().setPacketHandled(true);
        }

        private static void shrinkStack(ItemStack stack) {
            if (!stack.isEmpty()) stack.shrink(1);
        }
    }
}
