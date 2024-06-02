package svenhjol.strange.mixin;

import svenhjol.charm.charmony.MixinConfigPlugin;
import svenhjol.charm.charmony.enums.Side;
import svenhjol.charm.charmony.helper.DebugHelper;
import svenhjol.strange.Strange;

import java.util.List;

public class MixinConfig extends MixinConfigPlugin {
    @Override
    protected String id() {
        return Strange.ID;
    }

    @Override
    public List<Side> sides() {
        return List.of(Side.CLIENT, Side.COMMON);
    }

    @Override
    public boolean baseNameCheck(String baseName, String mixinClassName) {
        // With compat enabled we don't load ANY mixins EXCEPT accessors.
        if (DebugHelper.isCompatEnabled() && !baseName.equals("accessor")) {
            LOGGER.warn("Compat mode skipping mixin {}", mixinClassName);
            return false;
        }
        return true;
    }

    @Override
    public void consoleOutput(boolean isValid, String mixinClassName) {
        if (DebugHelper.isDebugEnabled()) {
            if (isValid) {
                LOGGER.info("Enabled mixin {}", mixinClassName);
            } else {
                LOGGER.warn("Disabled mixin {}", mixinClassName);
            }
        }
    }
}
