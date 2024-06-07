package svenhjol.strange.feature.core;

import svenhjol.charm.charmony.annotation.Feature;
import svenhjol.charm.charmony.client.ClientFeature;
import svenhjol.charm.charmony.client.ClientLoader;

@Feature
public final class CoreClient extends ClientFeature {
    public CoreClient(ClientLoader loader) {
        super(loader);
    }
}
