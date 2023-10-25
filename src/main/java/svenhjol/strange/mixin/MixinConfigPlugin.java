package svenhjol.strange.mixin;

import svenhjol.charmony.base.CharmonyMixinConfigPlugin;
import svenhjol.strange.Strange;

public class MixinConfigPlugin extends CharmonyMixinConfigPlugin {
    @Override
    protected String getModId() {
        return Strange.ID;
    }
}
