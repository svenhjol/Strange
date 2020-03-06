package svenhjol.strange.base.module;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.message.ServerUpdatePlayerState;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.BASE, hasSubscriptions = true,
    description = "Client to server communication options.\n" +
        "If disabled, ambient sounds won't work properly as the client won't be able to access extra world state.")
public class Base extends MesonModule
{
    @Config(name = "Client/Server update ticks", description = "Heartbeat interval to synchronise additional world state to the client.")
    public static int clientServerUpdateTicks = 120;

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END
            && event.player.world.isRemote
            && event.player.world.getGameTime() % clientServerUpdateTicks == 0
        ) {
            Meson.getInstance(Strange.MOD_ID).getPacketHandler().sendToServer(new ServerUpdatePlayerState());
        }
    }
}
