package svenhjol.strange.module.floating_islands_dimension;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;

@ClientModule(module = FloatingIslandsDimension.class)
public class FloatingIslandsDimensionClient extends CharmModule {
    @Override
    public ResourceLocation getId() {
        return FloatingIslandsDimension.ID;
    }

    @Override
    public void register() {
        DimensionSpecialEffects.EFFECTS.put(FloatingIslandsDimension.ID, new FloatingIslandsEffects());
    }
}
