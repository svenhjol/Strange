package svenhjol.strange.traveljournal.message;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import svenhjol.meson.helper.ClientHelper;
import svenhjol.meson.iface.IMesonMessage;
import svenhjol.strange.base.StrangeSounds;
import svenhjol.strange.traveljournal.Entry;
import svenhjol.strange.traveljournal.client.screen.ScreenshotScreen;
import svenhjol.strange.traveljournal.client.screen.UpdateEntryScreen;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClientActionMessage implements IMesonMessage
{
    public static final int SCREENSHOT = 0;
    public static final int ADD = 1;

    public int action;
    public String id;
    public String name;
    public BlockPos pos;
    public int dim;
    public int color;
    public Hand hand;

    public ClientActionMessage(int action, Entry entry, Hand hand)
    {
        this(action, entry.id, entry.name, entry.pos, entry.dim, entry.color, hand);
    }

    public ClientActionMessage(int action, String id, String name, BlockPos pos, int dim, int color, Hand hand)
    {
        this.action = action;
        this.id = id;
        this.name = name;
        this.pos = pos;
        this.dim = dim;
        this.color = color;
        this.hand = hand;
    }

    public static void encode(ClientActionMessage msg, PacketBuffer buf)
    {
        long pos = msg.pos == null ? 0 : msg.pos.toLong();

        buf.writeInt(msg.action);
        buf.writeString(msg.id);
        buf.writeString(msg.name);
        buf.writeLong(pos);
        buf.writeInt(msg.dim);
        buf.writeInt(msg.color);
        buf.writeEnumValue(msg.hand);
    }

    public static ClientActionMessage decode(PacketBuffer buf)
    {
        int action = buf.readInt();
        String id = buf.readString();
        String title = buf.readString();
        BlockPos pos = BlockPos.fromLong(buf.readLong());
        int dim = buf.readInt();
        int color = buf.readInt();
        Hand hand = buf.readEnumValue(Hand.class);
        return new ClientActionMessage(action, id, title, pos, dim, color, hand);
    }

    public static class Handler
    {
        public static void handle(final ClientActionMessage msg, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                PlayerEntity player = ClientHelper.getClientPlayer();
                Entry entry = new Entry(msg.id, msg.name, msg.pos, msg.dim, msg.color);

                if (msg.action == ADD) {
                    Minecraft.getInstance().displayGuiScreen(new UpdateEntryScreen(entry, player, msg.hand));
                }
                if (msg.action == SCREENSHOT) {
                    takeScreenshot(msg.id, result -> {
                        player.playSound(StrangeSounds.SCREENSHOT, 1.0F, 1.0F);
                        Minecraft.getInstance().displayGuiScreen(new ScreenshotScreen(entry, player, msg.hand));
                    });
                }
            });
            ctx.get().setPacketHandled(true);
        }

        public static void takeScreenshot(String id, Consumer<ITextComponent> onFinish)
        {
            Minecraft mc = Minecraft.getInstance();
            MainWindow win = mc.mainWindow;
            ScreenShotHelper.saveScreenshot(mc.gameDir, id, win.getFramebufferWidth() / 8, win.getFramebufferHeight() / 8, mc.getFramebuffer(), onFinish);
        }
    }
}
