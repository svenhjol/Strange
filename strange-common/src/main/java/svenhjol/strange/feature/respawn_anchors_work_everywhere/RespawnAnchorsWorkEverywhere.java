package svenhjol.strange.feature.respawn_anchors_work_everywhere;

import svenhjol.charm.Charm;
import svenhjol.charm_core.annotation.Feature;
import svenhjol.charm_core.base.CharmFeature;

@Feature(mod = Charm.MOD_ID, enabledByDefault = false, description = "The repsawn anchor can be used in any dimension.\n" +
    "This is an opinionated feature that changes core gameplay and so is disabled by default.")
public class RespawnAnchorsWorkEverywhere extends CharmFeature {
}
