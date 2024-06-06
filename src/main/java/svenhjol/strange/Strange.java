package svenhjol.strange;

import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.charmony.common.CommonFeature;
import svenhjol.strange.feature.runestones.Runestones;
import svenhjol.strange.feature.stone_circles.StoneCircles;
import svenhjol.strange.feature.travel_journals.TravelJournals;

import java.util.List;

public final class Strange {
    public static final String ID = "strange";

    public static ResourceLocation id(String path) {
        return new ResourceLocation(ID, path);
    }

    public static List<Class<? extends CommonFeature>> features() {
        return List.of(
            Runestones.class,
            StoneCircles.class,
            TravelJournals.class
        );
    }
}
