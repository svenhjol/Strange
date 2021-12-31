package svenhjol.strange.module.dimensions.mirror.network;

import net.minecraft.server.MinecraftServer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;
import svenhjol.strange.module.dimensions.mirror.MirrorDimension;

@Id("strange:mirror_weather_change")
public class ServerSendWeatherChange extends ServerSender {
    public void send(MinecraftServer server, MirrorDimension.WeatherPhase weather) {
        super.sendToAll(server, buf -> buf.writeEnum(weather));
    }
}
