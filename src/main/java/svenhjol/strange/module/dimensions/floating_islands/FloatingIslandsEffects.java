package svenhjol.strange.module.dimensions.floating_islands;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class FloatingIslandsEffects extends DimensionSpecialEffects {
    protected final float[] sunriseCol = new float[4];

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

    @Nullable
    @Override
    public float[] getSunriseColor(float f, float g) {
        float i = Mth.cos(f * ((float)Math.PI * 2)) - 0.0f;
        if (i >= -0.4f && i <= 0.4f) {
            float k = (i - -0.3f) / 0.4f * 0.5f + 0.5f;
            float l = 1.0f - (1.0f - Mth.sin(k * (float)Math.PI)) * 0.99f;
            l *= l;
            this.sunriseCol[0] = k * 0.4f + 0.6f;
            this.sunriseCol[1] = k * k * 0.5f + 0.5f;
            this.sunriseCol[2] = k * k * 0.7f;
            this.sunriseCol[3] = l;
            return this.sunriseCol;
        }
        return null;
    }
}
