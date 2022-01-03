package svenhjol.strange.module.floating_islands_dimension;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charmonium.api.event.AddBiomeAmbienceCallback;

@ClientModule(module = FloatingIslandsDimension.class)
public class FloatingIslandsDimensionClient extends CharmModule {
    @Override
    public ResourceLocation getId() {
        return FloatingIslandsDimension.ID;
    }

    @Override
    public void register() {
        DimensionSpecialEffects.EFFECTS.put(FloatingIslandsDimension.ID, new FloatingIslandsEffects());
        AddBiomeAmbienceCallback.EVENT.register(this::handleCharmoniumAmbience);
    }

    private boolean handleCharmoniumAmbience(Level level) {
        return DimensionHelper.isDimension(level, FloatingIslandsDimension.ID);
    }
}
