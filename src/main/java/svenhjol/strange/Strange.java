package svenhjol.strange;

import svenhjol.charmony.base.Mods;
import svenhjol.charmony.common.CommonFeature;
import svenhjol.charmony.common.CommonMod;
import svenhjol.strange.feature.ambient_music_discs.AmbientMusicDiscs;
import svenhjol.strange.feature.core.Core;
import svenhjol.strange.feature.ebony_wood.EbonyWood;
import svenhjol.strange.feature.piglin_pointing.PiglinPointing;
import svenhjol.strange.feature.raid_horns.RaidHorns;
import svenhjol.strange.feature.waypoints.Waypoints;

import java.util.List;

public class Strange extends CommonMod {
    public static final String ID = "strange";
    public static final String CHARM_ID = "charm";

    @Override
    public String modId() {
        return ID;
    }

    @Override
    public List<Class<? extends CommonFeature>> features() {
        return List.of(
            AmbientMusicDiscs.class,
            Core.class,
            EbonyWood.class,
            PiglinPointing.class,
            RaidHorns.class,
            Waypoints.class
        );
    }

    public static boolean isCharmFeatureEnabled(String featureName) {
        return Mods.optionalCommon(CHARM_ID).map(
            charm -> charm.loader().isEnabled(featureName)).orElse(false);
    }
}
