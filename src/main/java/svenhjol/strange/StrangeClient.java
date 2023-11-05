package svenhjol.strange;

import svenhjol.strange.feature.piglin_pointing.PiglinPointingClient;
import svenhjol.charmony.client.ClientFeature;
import svenhjol.charmony.client.ClientMod;
import svenhjol.strange.feature.ambient_music_discs.AmbientMusicDiscsClient;
import svenhjol.strange.feature.raid_horns.RaidHornsClient;

import java.util.List;

public class StrangeClient extends ClientMod {

    @Override
    public String modId() {
        return Strange.ID;
    }

    @Override
    public List<Class<? extends ClientFeature>> features() {
        return List.of(
            AmbientMusicDiscsClient.class,
            PiglinPointingClient.class,
            RaidHornsClient.class
        );
    }
}
