package svenhjol.strange.traveljournal.message;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.I18n;
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
import svenhjol.meson.handler.PlayerQueueHandler;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.totems.item.TotemOfReturningItem;
import svenhjol.strange.totems.module.TotemOfReturning;
import svenhjol.strange.traveljournal.Entry;
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

    public ActionMessage(int action, Entry entry, Hand hand)
    {
        this(action, entry.id, entry.name, entry.pos, entry.dim, entry.color, hand);
    }

    private ActionMessage(int action, String id, String name, BlockPos pos, int dim, int color, Hand hand)
    {
        this.action = action;
        this.hand = hand;
        this.id = id;
        this.name = name;
        this.pos = pos;
        this.dim = dim;
        this.color = color;
    }

    public static void encode(ActionMessage msg, PacketBuffer buf)
    {
        String name = msg.name == null ? "" : msg.name;
        long pos = msg.pos == null ? 0 : msg.pos.toLong();

        buf.writeInt(msg.action);
        buf.writeString(msg.id);
        buf.writeString(name);
        buf.writeLong(pos);
        buf.writeInt(msg.dim);
        buf.writeInt(msg.color);
        buf.writeEnumValue(msg.hand);
    }

    public static ActionMessage decode(PacketBuffer buf)
    {
        return new ActionMessage(
            buf.readInt(),
            buf.readString(),
            buf.readString(),
            BlockPos.fromLong(buf.readLong()),
            buf.readInt(),
            buf.readInt(),
            buf.readEnumValue(Hand.class)
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

                if (msg.name == null || msg.name.isEmpty()) {
                    msg.name = I18n.format("travel_journal.strange.new_entry");
                }

                // create entry
                Entry entry = new Entry(msg.id, msg.name, msg.pos, msg.dim, msg.color);

                if (msg.action == ADD) {

                    entries = TravelJournalItem.addEntry(held, entry);
                    updateAfterAdd(entry, msg.hand, player);

                } else if (msg.action == UPDATE) {

                    entries = TravelJournalItem.updateEntry(held, entry);

                } else if (msg.action == DELETE) {

                    entries = TravelJournalItem.deleteEntry(held, entry);

                } else if (msg.action == SCREENSHOT) {

                    CompoundNBT nbt = TravelJournalItem.getEntry(held, entry.id);
                    if (nbt != null && !nbt.isEmpty()) {
                        takeScreenshot(entry, msg.hand, player);
                    }

                } else if (msg.action == TELEPORT) {

                    CompoundNBT nbt = TravelJournalItem.getEntry(held, entry.id);
                    if (nbt != null && !nbt.isEmpty()) {
                        ItemStack totem = getTotem(player);
                        if (totem != null) {
                            TotemOfReturningItem.teleport(player.world, player, entry.pos, entry.dim, totem);
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

        private static void updateAfterAdd(Entry entry, Hand hand, PlayerEntity player)
        {
            PacketHandler.sendTo(new ClientActionMessage(ClientActionMessage.ADD, entry, hand), (ServerPlayerEntity)player);
        }

        private static void takeScreenshot(Entry entry, Hand hand, PlayerEntity player)
        {
            PlayerQueueHandler.add(player.world.getGameTime(), player, (p) -> {
                PacketHandler.sendTo(new ClientActionMessage(ClientActionMessage.SCREENSHOT, entry, hand), (ServerPlayerEntity)player);
            });
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
