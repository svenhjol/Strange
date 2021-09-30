package svenhjol.strange.module.dimensions;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.phys.Vec3;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unused")
@CommonModule(mod = Strange.MOD_ID)
public class Dimensions extends CharmModule {
    public static final ResourceLocation DARKLAND_ID = new ResourceLocation(Strange.MOD_ID, "darkland");
    public static final Map<ResourceLocation, Integer> FOG_COLOR = new HashMap<>();
    public static final Map<ResourceLocation, Integer> SKY_COLOR = new HashMap<>();
    public static final Map<ResourceLocation, Integer> WATER_COLOR = new HashMap<>();
    public static final Map<ResourceLocation, Integer> WATER_FOG_COLOR = new HashMap<>();
    public static final Map<ResourceLocation, Boolean> THUNDERING = new HashMap<>();
    public static final Map<ResourceLocation, Float> TEMPERATURE = new HashMap<>();
    public static final Map<ResourceLocation, Music> MUSIC = new HashMap<>();
    public static final Map<ResourceLocation, AmbientParticleSettings> AMBIENT_PARTICLE = new HashMap<>();

//    private static final ResourceKey<LevelStem> DARKLAND_DIMENSION = ResourceKey.create(
//        Registry.LEVEL_STEM_REGISTRY,
//        DARKLAND_ID
//    );
//    private static final ResourceKey<DimensionType> DARKLAND_DIMENSION_TYPE = ResourceKey.create(
//        Registry.DIMENSION_TYPE_REGISTRY,
//        new ResourceLocation(Strange.MOD_ID, "darkland_type")
//    );
//    public static ResourceKey<Level> DARKLAND = ResourceKey.create(
//        Registry.DIMENSION_REGISTRY,
//        DARKLAND_DIMENSION.location()
//    );

    @Override
    public void register() {
//        Dimensions.DARKLAND = ResourceKey.create(Registry.DIMENSION_REGISTRY, Dimensions.DARKLAND_ID);
//        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(Strange.MOD_ID, "darkland"), NoiseBasedChunkGenerator.CODEC);


        Dimensions.SKY_COLOR.put(Dimensions.DARKLAND_ID, 0x000000);
        Dimensions.FOG_COLOR.put(Dimensions.DARKLAND_ID, 0x004434);
        Dimensions.WATER_COLOR.put(Dimensions.DARKLAND_ID, 0x102020);
        Dimensions.WATER_FOG_COLOR.put(Dimensions.DARKLAND_ID, 0x102020);
        Dimensions.THUNDERING.put(Dimensions.DARKLAND_ID, true);
        Dimensions.TEMPERATURE.put(Dimensions.DARKLAND_ID, 0.0F);
        Dimensions.AMBIENT_PARTICLE.put(Dimensions.DARKLAND_ID, new AmbientParticleSettings(ParticleTypes.WHITE_ASH, 0.118093334F));
        Dimensions.MUSIC.put(Dimensions.DARKLAND_ID, Musics.createGameMusic(SoundEvents.MUSIC_BIOME_BASALT_DELTAS));
    }

    @Override
    public void runWhenEnabled() {
        ServerTickEvents.END_WORLD_TICK.register(this::handleWorldTick);
    }

    public static Optional<Float> getTemperature(LevelReader levelReader, Biome biome) {
        if (levelReader instanceof Level)
            return Optional.ofNullable(TEMPERATURE.get(DimensionHelper.getDimension((Level)levelReader)));

        return Optional.empty();
    }

    public static Optional<Boolean> isThundering(Level level) {
        return Optional.ofNullable(THUNDERING.get(DimensionHelper.getDimension(level)));
    }

    private void handleWorldTick(Level level) {
        isThundering(level).ifPresent(alwaysThundering -> {
            if (level.isClientSide || !alwaysThundering) return;
            ServerLevel serverLevel = (ServerLevel) level;

            if (serverLevel.random.nextInt(800) == 0) {
                ServerPlayer player = ((ServerLevel) level).getRandomPlayer();
                if (player == null) return;

                int dist = 140;
                BlockPos pos = player.blockPosition();
                int x = level.random.nextInt(dist);
                int z = level.random.nextInt(dist);
                BlockPos lightningPos = serverLevel.findLightningTargetAround(new BlockPos(pos.getX() + (dist/2) - x, pos.getY(), pos.getZ() + (dist/2) - z));
                if (!level.isLoaded(lightningPos)) return;

                /** @see ServerLevel#tickChunk */
                LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                if (lightningBolt == null) return;
                
                lightningBolt.moveTo(Vec3.atBottomCenterOf(lightningPos));
                lightningBolt.setVisualOnly(false);
                serverLevel.addFreshEntity(lightningBolt);
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
