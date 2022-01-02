package svenhjol.strange.module.dimensions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;

import java.util.*;

/**
 * @link {https://misode.github.io/worldgen/}
 */
@SuppressWarnings("unused")
@CommonModule(mod = Strange.MOD_ID, description = "Supports additional dimensions and their unique effects.\n" +
    "If this feature is disabled, all custom dimensions will be disabled.")
public class Dimensions extends CharmModule {
    public static final Map<ResourceLocation, Integer> FOG_COLOR = new HashMap<>();
    public static final Map<ResourceLocation, Integer> SKY_COLOR = new HashMap<>();
    public static final Map<ResourceLocation, Integer> GRASS_COLOR = new HashMap<>();
    public static final Map<ResourceLocation, Integer> FOLIAGE_COLOR = new HashMap<>();
    public static final Map<ResourceLocation, Integer> WATER_COLOR = new HashMap<>();
    public static final Map<ResourceLocation, Integer> WATER_FOG_COLOR = new HashMap<>();
    public static final Map<ResourceLocation, Float> RAIN_LEVEL = new HashMap<>();
    public static final Map<ResourceLocation, Float> TEMPERATURE = new HashMap<>();
    public static final Map<ResourceLocation, Double> HORIZON_HEIGHT = new HashMap<>();
    public static final Map<ResourceLocation, Music> MUSIC = new HashMap<>();
    public static final Map<ResourceLocation, SoundEvent> AMBIENT_LOOP = new HashMap<>();
    public static final Map<ResourceLocation, AmbientAdditionsSettings> AMBIENT_ADDITIONS = new HashMap<>();
    public static final Map<ResourceLocation, Boolean> RENDER_PRECIPITATION = new HashMap<>();
    public static final Map<ResourceLocation, AmbientParticleSettings> AMBIENT_PARTICLE = new HashMap<>();
    public static final Map<ResourceLocation, Biome.Precipitation> PRECIPITATION = new HashMap<>();

    public static final ThreadLocal<LevelReader> LEVEL = new ThreadLocal<>();

    public static Optional<Float> getTemperature(LevelReader level, Biome biome) {
        if (level instanceof Level) {
            return Optional.ofNullable(TEMPERATURE.get(DimensionHelper.getDimension((Level) level)));
        }
        return Optional.empty();
    }

    public static Optional<Biome.Precipitation> getPrecipitation(LevelReader level) {
        if (level instanceof Level) {
            return Optional.ofNullable(PRECIPITATION.get(DimensionHelper.getDimension((Level) level)));
        }
        return Optional.empty();
    }

    public static Optional<Float> getRainLevel(Level level) {
        return Optional.ofNullable(RAIN_LEVEL.get(DimensionHelper.getDimension(level)));
    }

    public static final class SeedSupplier {
        public static final long MARKER = -1;

        public static long getSeed() {
            return MARKER;
        }
    }
}
