package svenhjol.strange.mixin;

import svenhjol.charm.mixin.BaseMixinConfigPlugin;
import svenhjol.strange.Strange;

public class StrangeMixinConfigPlugin extends BaseMixinConfigPlugin {
    @Override
    public String getModId() {
        return Strange.MOD_ID;
    }
}
