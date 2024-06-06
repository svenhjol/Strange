package svenhjol.strange.feature.stone_circles;

import net.minecraft.util.Mth;
import svenhjol.charm.charmony.annotation.Configurable;
import svenhjol.charm.charmony.annotation.Feature;
import svenhjol.charm.charmony.common.CommonFeature;
import svenhjol.charm.charmony.common.CommonLoader;
import svenhjol.strange.feature.stone_circles.common.Providers;
import svenhjol.strange.feature.stone_circles.common.Registers;

@Feature(description = """
    Adds small pillars of stone in all three dimensions.
    Runestones can often be found on the top of the stone pillars.""")
public final class StoneCircles extends CommonFeature {
    public static final String STRUCTURE_ID = "stone_circle";

    public final Registers registers;
    public final Providers providers;

    @Configurable(
        name = "Stone circle runestone chance",
        description = """
            Chance (out of 1.0) of a runestone linking to another stone circle.
            This chance is calculated only if the stone circle provider is used for the runestone block position."""
    )
    private static double stoneCircleRunestoneChance = 0.3d;

    public StoneCircles(CommonLoader loader) {
        super(loader);

        registers = new Registers(this);
        providers = new Providers(this);
    }

    public double stoneCircleRunestoneChance() {
        return Mth.clamp(stoneCircleRunestoneChance, 0.0d, 1.0d);
    }
}
