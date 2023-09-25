package svenhjol.strange.mixin;

import svenhjol.strange.Strange;
import svenhjol.charmony.base.CharmMixinConfigPlugin;

public class MixinConfigPlugin extends CharmMixinConfigPlugin {
    @Override
    protected String getModId() {
        return Strange.MOD_ID;
    }
}
