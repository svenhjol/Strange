package svenhjol.strange.module.dimensions;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.Music;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.event.AddEntityCallback;
import svenhjol.charm.event.PlayerTickCallback;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.dimensions.floating_islands.FloatingIslandsDimension;
import svenhjol.strange.module.dimensions.mirror.MirrorDimension;

import java.util.*;

/**
 * @see {https://misode.github.io/worldgen/}
 */
@SuppressWarnings("unused")
@CommonModule(mod = Strange.MOD_ID)
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
    public static final Map<ResourceLocation, Boolean> RENDER_PRECIPITATION = new HashMap<>();
    public static final Map<ResourceLocation, AmbientParticleSettings> AMBIENT_PARTICLE = new HashMap<>();
    public static final Map<ResourceLocation, Biome.Precipitation> PRECIPITATION = new HashMap<>();

    public static final List<IDimension> DIMENSIONS = new ArrayList<>();
    public static final ThreadLocal<LevelReader> LEVEL = new ThreadLocal<>();

    public static boolean loadMirrorDimension = true;
    public static boolean loadFloatingIslandsDimension = true;

    @Override
    public void register() {
        // add new dimensions to this list
        if (loadMirrorDimension) DIMENSIONS.add(new MirrorDimension());
        if (loadFloatingIslandsDimension) DIMENSIONS.add(new FloatingIslandsDimension());

        // register all dimensions
        DIMENSIONS.forEach(IDimension::register);
    }

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(this::handleWorldLoad);
        ServerTickEvents.END_WORLD_TICK.register(this::handleWorldTick);
        PlayerTickCallback.EVENT.register(this::handlePlayerTick);
        AddEntityCallback.EVENT.register(this::handleAddEntity);
    }

    private void handleWorldLoad(MinecraftServer server, ServerLevel level) {
        DIMENSIONS.forEach(d -> {
            if (level.dimension().location().equals(d.getId())) {
                d.handleWorldLoad(server, level);
            }
        });
    }

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

    private void handleWorldTick(Level level) {
        DIMENSIONS.forEach(d -> {
            if (level.dimension().location().equals(d.getId())) {
                d.handleWorldTick(level);
            }
        });
    }

    private InteractionResult handleAddEntity(Entity entity) {
        for (IDimension dimension : DIMENSIONS) {
            if (entity.level.dimension().location().equals(dimension.getId())) {
                InteractionResult result = dimension.handleAddEntity(entity);
                if (result == InteractionResult.FAIL) {
                    return InteractionResult.FAIL;
                }
            }
        }
        return InteractionResult.PASS;
    }

    private void handlePlayerTick(Player player) {
        DIMENSIONS.forEach(d -> {
            if (player.level.dimension().location().equals(d.getId())) {
                d.handlePlayerTick(player);
            }
        });
    }

    public static final class SeedSupplier {
        public static final long MARKER = -1;

        public static long getSeed() {
            return MARKER;
        }
    }
}
