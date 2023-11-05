package svenhjol.strange;

import svenhjol.strange.feature.piglin_pointing.PiglinPointing;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.charmony.common.CommonMod;
import svenhjol.strange.feature.ambient_music_discs.AmbientMusicDiscs;
import svenhjol.strange.feature.raid_horns.RaidHorns;

import java.util.List;

public class Strange extends CommonMod {
    public static final String ID = "strange";

    @Override
    public String modId() {
        return ID;
    }

    @Override
    public List<Class<? extends CommonFeature>> features() {
        return List.of(
            AmbientMusicDiscs.class,
            PiglinPointing.class,
            RaidHorns.class
        );
    }
}
