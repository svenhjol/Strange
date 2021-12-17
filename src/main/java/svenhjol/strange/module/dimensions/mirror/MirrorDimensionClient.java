package svenhjol.strange.module.dimensions.mirror;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import svenhjol.strange.module.dimensions.IDimensionClient;

public class MirrorDimensionClient implements IDimensionClient {
    @Override
    public void register() {
        DimensionSpecialEffects.EFFECTS.put(MirrorDimension.ID, new MirrorEffects());
    }
}
