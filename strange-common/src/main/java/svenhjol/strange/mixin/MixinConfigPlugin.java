package svenhjol.strange.mixin;

import svenhjol.charm_core.base.BaseMixinConfigPlugin;
import svenhjol.strange.Strange;

public class MixinConfigPlugin extends BaseMixinConfigPlugin {
    @Override
    protected String getModId() {
        return Strange.MOD_ID;
    }
}
