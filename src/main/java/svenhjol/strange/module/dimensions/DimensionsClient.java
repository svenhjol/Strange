package svenhjol.strange.module.dimensions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.loader.CharmModule;

import java.util.Optional;

@SuppressWarnings("unused")
@ClientModule(module = Dimensions.class)
public class DimensionsClient extends CharmModule {
    public static Optional<Integer> getSkyColor(Biome biome) {
        return Optional.ofNullable(Dimensions.SKY_COLOR.get(getDimension()));
    }

    public static Optional<Integer> getFogColor(Biome biome) {
        return Optional.ofNullable(Dimensions.FOG_COLOR.get(getDimension()));
    }

    public static Optional<Integer> getWaterColor(Biome biome) {
        return Optional.ofNullable(Dimensions.WATER_COLOR.get(getDimension()));
    }

    public static Optional<Integer> getWaterFogColor(Biome biome) {
        return Optional.ofNullable(Dimensions.WATER_FOG_COLOR.get(getDimension()));
    }

    public static Optional<Integer> getGrassColor(Biome biome) {
        return Optional.ofNullable(Dimensions.GRASS_COLOR.get(getDimension()));
    }

    public static Optional<Integer> getFoliageColor(Biome biome) {
        return Optional.ofNullable(Dimensions.FOLIAGE_COLOR.get(getDimension()));
    }

    public static Optional<AmbientParticleSettings> getAmbientParticle(Biome biome) {
        return Optional.ofNullable(Dimensions.AMBIENT_PARTICLE.get(getDimension()));
    }

    public static Optional<Music> getMusic(Biome biome) {
        return Optional.ofNullable(Dimensions.MUSIC.get(getDimension()));
    }

    public static Optional<SoundEvent> getAmbientLoop(Biome biome) {
        return Optional.ofNullable(Dimensions.AMBIENT_LOOP.get(getDimension()));
    }

    public static Optional<AmbientAdditionsSettings> getAmbientAdditions(Biome biome) {
        return Optional.ofNullable(Dimensions.AMBIENT_ADDITIONS.get(getDimension()));
    }

    public static Optional<Double> getHorizonHeight(LevelHeightAccessor levelHeight) {
        return Optional.ofNullable(Dimensions.HORIZON_HEIGHT.get(getDimension()));
    }

    public static Optional<Boolean> shouldRenderPrecipitation() {
        return Optional.ofNullable(Dimensions.RENDER_PRECIPITATION.get(getDimension()));
    }

    private static ResourceLocation getDimension() {
        return DimensionHelper.getDimension(ClientHelper.getLevelKey().orElse(Level.OVERWORLD));
    }
}
