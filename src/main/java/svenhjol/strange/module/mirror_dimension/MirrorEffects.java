package svenhjol.strange.module.mirror_dimension;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

public class MirrorEffects extends DimensionSpecialEffects {
    /**
     * - cloud level
     * - has ground
     * - sky type
     * - force bright lightmap
     * - constant ambient light
     */
    public MirrorEffects() {
        super(Float.NaN, true, SkyType.NONE, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float f) {
        return vec3;
    }

    @Override
    public boolean isFoggyAt(int i, int j) {
        return true;
    }
}
