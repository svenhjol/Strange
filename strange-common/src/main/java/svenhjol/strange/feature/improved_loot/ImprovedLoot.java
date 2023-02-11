package svenhjol.strange.feature.improved_loot;

import svenhjol.charm.Charm;
import svenhjol.charm_core.annotation.Configurable;
import svenhjol.charm_core.annotation.Feature;
import svenhjol.charm_core.base.CharmFeature;

/**
 * @todo Make this use custom loot tables.
 */
@Feature(mod = Charm.MOD_ID, description = "Adds items to woodland mansion and nether fortress loot chests.")
public class ImprovedLoot extends CharmFeature {
    private Fortress FORTRESS;
    private Mansion MANSION;

    @Configurable(name = "Fortress loot", description = "Adds enchanted golden items, diamonds and blaze powder to nether fortresses.")
    public static boolean fortressLoot = true;

    @Configurable(name = "Mansion loot", description = "Adds weapons, emeralds, totems and enchanted books to woodland mansions.")
    public static boolean mansionLoot = true;

    @Override
    public void register() {
        FORTRESS = new Fortress();
        MANSION = new Mansion();

        FORTRESS.register();
        MANSION.register();
    }

    @Override
    public void runWhenEnabled() {
        if (fortressLoot) {
            FORTRESS.runWhenEnabled();
        }

        if (mansionLoot) {
            MANSION.runWhenEnabled();
        }
    }
}
