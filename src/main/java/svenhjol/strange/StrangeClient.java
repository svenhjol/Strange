package svenhjol.strange;

import svenhjol.charm.charmony.client.ClientFeature;
import svenhjol.strange.feature.runestones.RunestonesClient;

import java.util.List;

public final class StrangeClient {
    public static List<Class<? extends ClientFeature>> features() {
        return List.of(
            RunestonesClient.class
        );
    }
}
