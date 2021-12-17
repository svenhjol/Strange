package svenhjol.strange.module.dimensions.floating_islands;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import svenhjol.strange.module.dimensions.IDimensionClient;

public class FloatingIslandsDimensionClient implements IDimensionClient {
    @Override
    public void register() {
        DimensionSpecialEffects.EFFECTS.put(FloatingIslandsDimension.ID, new FloatingIslandsEffects());
    }
}
