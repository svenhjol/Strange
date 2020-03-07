package svenhjol.strange.traveljournal.message;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.charm.tools.item.BoundCompassItem;
import svenhjol.charm.tools.module.CompassBinding;
import svenhjol.meson.Meson;
import svenhjol.meson.handler.PlayerQueueHandler;
import svenhjol.meson.helper.PlayerHelper;
import svenhjol.meson.helper.StringHelper;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.Strange;
import svenhjol.strange.totems.item.TotemOfReturningItem;
import svenhjol.strange.totems.module.TotemOfReturning;
import svenhjol.strange.traveljournal.Entry;
import svenhjol.strange.traveljournal.item.TravelJournalItem;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ServerTravelJournalAction implements IMesonMessage {
    public static final int ADD = 0;
    public static final int UPDATE = 1;
    public static final int DELETE = 2;
    public static final int TELEPORT = 3;
    public static final int SCREENSHOT = 4;
    public static final int BIND_COMPASS = 5;
    public static final int MAKE_MAP = 6;

    private String id;
    private BlockPos pos;
    private Hand hand;
    private int action;
    private int color;
    private int dim;
    private String name;

    public ServerTravelJournalAction(int action, Entry entry, Hand hand) {
        this(action, entry.id, entry.name, entry.pos, entry.dim, entry.color, hand);
    }

    private ServerTravelJournalAction(int action, String id, String name, BlockPos pos, int dim, int color, Hand hand) {
        this.action = action;
        this.hand = hand;
        this.id = id;
        this.name = name;
        this.pos = pos;
        this.dim = dim;
        this.color = color;
    }

    public static void encode(ServerTravelJournalAction msg, PacketBuffer buf) {
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

    public static ServerTravelJournalAction decode(PacketBuffer buf) {
        return new ServerTravelJournalAction(
            buf.readInt(),
            buf.readString(32),
            buf.readString(32),
            BlockPos.fromLong(buf.readLong()),
            buf.readInt(),
            buf.readInt(),
            buf.readEnumValue(Hand.class)
        );
    }

    public static class Handler {
        public static void handle(final ServerTravelJournalAction msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                World world;
                ItemStack held;

                NetworkEvent.Context context = ctx.get();
                ServerPlayerEntity player = context.getSender();
                if (player == null) return;

                world = player.world;
                held = player.getHeldItem(msg.hand);

                if (msg.name == null || msg.name.isEmpty()) {
                    msg.name = new TranslationTextComponent("gui.strange.travel_journal.new_entry").getUnformattedComponentText();
                }

                // create entry
                Entry entry = new Entry(msg.id, msg.name, msg.pos, msg.dim, msg.color);
                CompoundNBT nbt = TravelJournalItem.getEntry(held, entry.id);
                ImmutableList<NonNullList<ItemStack>> inventories = PlayerHelper.getInventories(player);

                if (msg.action == ADD) {

                    TravelJournalItem.addEntry(held, entry);

                } else if (msg.action == UPDATE) {

                    TravelJournalItem.updateEntry(held, entry);

                } else if (msg.action == DELETE) {

                    TravelJournalItem.deleteEntry(held, entry);

                } else if (msg.action == SCREENSHOT) {

                    if (nbt != null && !nbt.isEmpty()) {
                        takeScreenshot(entry, msg.hand, player);
                    }

                } else if (msg.action == TELEPORT) {

                    if (nbt != null && !nbt.isEmpty()) {
                        ItemStack totem = getItemFromInventory(inventories, TotemOfReturning.item);
                        if (totem != null) {
                            TotemOfReturningItem.teleport(player.world, player, entry.pos, entry.dim, totem);
                        }
                    }

                } else if (msg.action == BIND_COMPASS) {

                    if (nbt != null && !nbt.isEmpty()) {
                        ItemStack compass = getItemFromInventory(inventories, Items.COMPASS);
                        if (compass != null) {
                            compass.shrink(1);

                            ItemStack boundCompass = new ItemStack(CompassBinding.item);
                            BoundCompassItem.setPos(boundCompass, entry.pos);
                            BoundCompassItem.setDim(boundCompass, entry.dim);
                            BoundCompassItem.setColor(boundCompass, entry.color);

                            StringTextComponent text = new StringTextComponent(entry.name);
                            text.setStyle(new Style().setColor(StringHelper.getTextFormattingByDyeDamage(entry.color)));
                            boundCompass.setDisplayName(text);

                            PlayerHelper.addOrDropStack(player, boundCompass);
                        }
                    }

                } else if (msg.action == MAKE_MAP) {

                    if (nbt != null && !nbt.isEmpty()) {
                        ItemStack map = getItemFromInventory(inventories, Items.MAP);
                        if (map != null) {
                            DyeColor col = DyeColor.byId(entry.color);
                            MapDecoration.Type decoration = MapDecoration.Type.TARGET_X;

                            if (col == DyeColor.BLACK) decoration = MapDecoration.Type.BANNER_BLACK;
                            if (col == DyeColor.BLUE) decoration = MapDecoration.Type.BANNER_BLUE;
                            if (col == DyeColor.PURPLE) decoration = MapDecoration.Type.BANNER_PURPLE;
                            if (col == DyeColor.RED) decoration = MapDecoration.Type.BANNER_RED;
                            if (col == DyeColor.BROWN) decoration = MapDecoration.Type.BANNER_BROWN;
                            if (col == DyeColor.GREEN) decoration = MapDecoration.Type.BANNER_GREEN;
                            if (col == DyeColor.LIGHT_GRAY) decoration = MapDecoration.Type.BANNER_LIGHT_GRAY;

                            map.shrink(1);
                            BlockPos pos = entry.pos;
                            ItemStack filled = FilledMapItem.setupNewMap(world, pos.getX(), pos.getZ(), (byte) 2, true, true);
                            FilledMapItem.renderBiomePreviewMap(world, filled);
                            MapData.addTargetDecoration(filled, pos, "+", decoration);

                            StringTextComponent text = new StringTextComponent(entry.name);
                            text.setStyle(new Style().setColor(StringHelper.getTextFormattingByDyeDamage(entry.color)));
                            filled.setDisplayName(text);

                            PlayerHelper.addOrDropStack(player, filled);
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }

        private static void takeScreenshot(Entry entry, Hand hand, PlayerEntity player) {
            PlayerQueueHandler.add(player.world.getGameTime() + 60, player, (p) -> {
                player.sendStatusMessage(new StringTextComponent(""), true);
                Meson.getInstance(Strange.MOD_ID).getPacketHandler().sendTo(new ClientTravelJournalAction(ClientTravelJournalAction.SCREENSHOT, entry, hand), (ServerPlayerEntity) player);
            });
        }

        @Nullable
        private static ItemStack getItemFromInventory(ImmutableList<NonNullList<ItemStack>> inventories, Item item) {
            for (NonNullList<ItemStack> itemStacks : inventories) {
                for (ItemStack stack : itemStacks) {
                    if (!stack.isEmpty() && stack.getItem() == item) {
                        return stack;
                    }
                }
            }
            return null;
        }
    }
}
