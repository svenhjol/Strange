package svenhjol.strange.module.mirror_dimension.network;

import net.minecraft.server.level.ServerPlayer;
import svenhjol.charm.network.Id;
import svenhjol.charm.network.ServerSender;
import svenhjol.strange.module.mirror_dimension.MirrorDimension;

@Id("strange:mirror_weather_ticks")
public class ServerSendWeatherTicks extends ServerSender {
    public void send(ServerPlayer player, MirrorDimension.WeatherPhase weather, int ticks) {
        super.send(player, buf -> {
            buf.writeEnum(weather);
            buf.writeInt(ticks);
        });
    }
}