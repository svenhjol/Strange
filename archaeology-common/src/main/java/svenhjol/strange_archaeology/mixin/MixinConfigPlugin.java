package svenhjol.strange_archaeology.mixin;

import svenhjol.charm_core.base.BaseMixinConfigPlugin;
import svenhjol.strange_archaeology.StrangeArchaeology;

public class MixinConfigPlugin extends BaseMixinConfigPlugin {
    @Override
    protected String getModId() {
        return StrangeArchaeology.MOD_ID;
    }
}
