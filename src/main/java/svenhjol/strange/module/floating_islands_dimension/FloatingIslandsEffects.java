package svenhjol.strange.module.floating_islands_dimension;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

public class FloatingIslandsEffects extends DimensionSpecialEffects {
    /**
     * - cloud level
     * - has ground
     * - sky type
     * - force bright lightmap
     * - constant ambient light
     */
    public FloatingIslandsEffects() {
        super(0, true, SkyType.NORMAL, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
        /** copypasta from {@link net.minecraft.client.renderer.DimensionSpecialEffects.OverworldEffects#getBrightnessDependentFogColor} */
        return vec3.multiply(f * 0.94f + 0.06f, f * 0.94f + 0.06f, f * 0.91f + 0.09f);
    }

    @Override
    public boolean isFoggyAt(int i, int j) {
        return false;
    }
}
