package svenhjol.strange.traveljournal.message;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.totems.item.TotemOfReturningItem;
import svenhjol.strange.totems.module.TotemOfReturning;
import svenhjol.strange.traveljournal.item.TravelJournalItem;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ActionMessage implements IMesonMessage
{
    public static final int ADD = 0;
    public static final int UPDATE = 1;
    public static final int DELETE = 2;
    public static final int TELEPORT = 3;
    public static final int SCREENSHOT = 4;

    private String id;
    private BlockPos pos;
    private Hand hand;
    private int action;
    private int color;
    private int dim;
    private String name;

    public ActionMessage(int action, String id, Hand hand)
    {
        this.id = id;
        this.action = action;
        this.hand = hand;
    }

    public ActionMessage(int action, String id, Hand hand, int color, int dim, @Nullable BlockPos pos, @Nullable String name)
    {
        this.id = id;
        this.action = action;
        this.hand = hand;
        this.pos = pos;
        this.dim = dim;
        this.color = color;
        this.name = name == null ? "" : name;
    }

    public static void encode(ActionMessage msg, PacketBuffer buf)
    {
        String name = msg.name == null ? "" : msg.name;
        long pos = msg.pos == null ? 0 : msg.pos.toLong();

        buf.writeInt(msg.action);
        buf.writeString(msg.id);
        buf.writeEnumValue(msg.hand);
        buf.writeInt(msg.color);
        buf.writeInt(msg.dim);
        buf.writeLong(pos);
        buf.writeString(name);
    }

    public static ActionMessage decode(PacketBuffer buf)
    {
        return new ActionMessage(
            buf.readInt(),
            buf.readString(),
            buf.readEnumValue(Hand.class),
            buf.readInt(),
            buf.readInt(),
            BlockPos.fromLong(buf.readLong()),
            buf.readString()
        );
    }

    public static class Handler
    {
        public static void handle(final ActionMessage msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                NetworkEvent.Context context = ctx.get();
                ServerPlayerEntity player = context.getSender();
                if (player == null) return;

                ItemStack held = player.getHeldItem(msg.hand);
                CompoundNBT entries = null;

                if (msg.action == ADD) {

                    entries = TravelJournalItem.addEntry(held, msg.id, msg.pos, msg.dim);

                } else if (msg.action == UPDATE) {

                    entries = TravelJournalItem.updateEntry(held, msg.id, msg.pos, msg.dim, msg.name, msg.color);

                } else if (msg.action == DELETE) {

                    entries = TravelJournalItem.deleteEntry(held, msg.id);

                } else if (msg.action == SCREENSHOT) {

                    CompoundNBT entry = TravelJournalItem.getEntry(held, msg.id);
                    if (entry != null) {
                        takeScreenshot(msg.id, entry.getString(TravelJournalItem.NAME), TravelJournalItem.getPos(entry), msg.hand, player);
                    }

                } else if (msg.action == TELEPORT) {

                    CompoundNBT entry = TravelJournalItem.getEntry(held, msg.id);
                    if (entry != null) {
                        ItemStack totem = getTotem(player);
                        if (totem != null) {
                            TotemOfReturningItem.teleport(player.world, player, TravelJournalItem.getPos(entry), totem);
                        }
                    }
                }

                if (entries != null) {
                    updateClientEntries(entries, player);
                }
            });
            ctx.get().setPacketHandled(true);
        }

        private static void updateClientEntries(CompoundNBT entries, PlayerEntity player)
        {
            PacketHandler.sendTo(new ClientEntriesMessage(entries), (ServerPlayerEntity)player);
        }

        private static void takeScreenshot(String id, String title, BlockPos pos, Hand hand, PlayerEntity player)
        {
            PacketHandler.sendTo(new ClientActionMessage(ClientActionMessage.SCREENSHOT, id, title, pos, hand), (ServerPlayerEntity)player);
        }

        @Nullable
        private static ItemStack getTotem(PlayerEntity player)
        {
            // check if player has a totem in their inventory
            PlayerInventory inventory = player.inventory;
            ImmutableList<NonNullList<ItemStack>> inventories = ImmutableList.of(inventory.mainInventory, inventory.offHandInventory);

            for (NonNullList<ItemStack> itemStacks : inventories) {
                for (ItemStack stack : itemStacks) {
                    if (!stack.isEmpty() && stack.getItem() == TotemOfReturning.item) {
                        return stack;
                    }
                }
            }

            return null;
        }
    }
}
