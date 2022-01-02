package svenhjol.strange.module.mirror_dimension.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import svenhjol.charm.network.ClientReceiver;
import svenhjol.charm.network.Id;
import svenhjol.strange.module.mirror_dimension.MirrorDimension;
import svenhjol.strange.module.mirror_dimension.MirrorDimensionClient;

@Id("strange:mirror_weather_change")
public class ClientReceiveWeatherChange extends ClientReceiver {
    @Override
    public void handle(Minecraft client, FriendlyByteBuf buffer) {
        var weather = buffer.readEnum(MirrorDimension.WeatherPhase.class);
        client.execute(() -> MirrorDimensionClient.handleWeatherChange(weather, 0));
    }
}
