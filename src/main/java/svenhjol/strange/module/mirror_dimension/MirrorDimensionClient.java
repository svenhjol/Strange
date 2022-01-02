package svenhjol.strange.module.mirror_dimension;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.dimensions.Dimensions;
import svenhjol.strange.module.intercept_music.InterceptMusic;
import svenhjol.strange.module.intercept_music.InterceptMusicClient;
import svenhjol.strange.module.intercept_music.MusicCondition;
import svenhjol.strange.module.mirror_dimension.network.ClientReceiveWeatherChange;
import svenhjol.strange.module.mirror_dimension.network.ClientReceiveWeatherTicks;

import java.util.Random;

@ClientModule(module = MirrorDimension.class)
public class MirrorDimensionClient extends CharmModule {
    public static ClientReceiveWeatherChange CLIENT_RECEIVE_WEATHER_CHANGE;
    public static ClientReceiveWeatherTicks CLIENT_RECEIVE_WEATHER_TICKS;

    public static final int startParticlesAfter = 750; // number of ticks between particle effects
    public static final int maxParticleTicks = 1000; // number of ticks that particle effects will occur

    public static MirrorDimension.WeatherPhase weatherPhase = MirrorDimension.WeatherPhase.CALM;
    public static int weatherPhaseTicks = 0;
    public static int particleTicks = startParticlesAfter;

    @Override
    public ResourceLocation getId() {
        return MirrorDimension.ID;
    }

    @Override
    public void register() {
        CLIENT_RECEIVE_WEATHER_CHANGE = new ClientReceiveWeatherChange();
        CLIENT_RECEIVE_WEATHER_TICKS = new ClientReceiveWeatherTicks();

        DimensionSpecialEffects.EFFECTS.put(MirrorDimension.ID, new MirrorEffects());

        if (Strange.LOADER.isEnabled(InterceptMusic.class)) {
            var ambientMusicCondition = new MusicCondition("mirror_music", 0, MirrorDimension.MIRROR_MUSIC, 12000, 24000, true, current -> {
                var mc = Minecraft.getInstance();
                if (mc == null || mc.player == null) return false;
                return mc.player.level.dimension().location().equals(MirrorDimension.ID);
            });

            InterceptMusicClient.addCondition(ambientMusicCondition);
        }
    }

    @Override
    public void runWhenEnabled() {
        ClientTickEvents.END_WORLD_TICK.register(this::handleWorldTick);
    }

    public void handleWorldTick(ClientLevel level) {
        Random random = level.getRandom();
        handleParticles(random);
        handleWeather(level, random);
    }

    public static void handleWeatherChange(MirrorDimension.WeatherPhase weather, int ticks) {
        weatherPhaseTicks = ticks;
        weatherPhase = weather;

        Dimensions.FOG_COLOR.put(MirrorDimension.ID, MirrorDimension.FOG);

        switch (weather) {
            case FREEZING -> {
                Dimensions.AMBIENT_LOOP.put(MirrorDimension.ID, MirrorDimension.MIRROR_FREEZING_LOOP);
                Dimensions.AMBIENT_ADDITIONS.put(MirrorDimension.ID, MirrorDimension.MIRROR_FREEZING_SETTINGS);
            }
            case SCORCHING -> {
                Dimensions.AMBIENT_LOOP.put(MirrorDimension.ID, MirrorDimension.MIRROR_SCORCHING_LOOP);
                Dimensions.AMBIENT_ADDITIONS.put(MirrorDimension.ID, MirrorDimension.MIRROR_SCORCHING_SETTINGS);
            }
            case STORMING, CALM -> {
                Dimensions.AMBIENT_LOOP.put(MirrorDimension.ID, MirrorDimension.MIRROR_AMBIENCE_LOOP);
                Dimensions.AMBIENT_ADDITIONS.put(MirrorDimension.ID, MirrorDimension.MIRROR_AMBIENCE_SETTINGS);
            }
        }

        ClientHelper.getPlayer().ifPresent(player -> {
            if (player instanceof LocalPlayer localPlayer) {
                localPlayer.ambientSoundHandlers.stream()
                    .filter(h -> h instanceof BiomeAmbientSoundsHandler)
                    .findFirst()
                    .ifPresent(handler -> {
                        var biomeHandler = (BiomeAmbientSoundsHandler) handler;
                        LogHelper.debug(Strange.MOD_ID, MirrorDimensionClient.class, "previousBiome is set to `" + biomeHandler.previousBiome + "`");
                        biomeHandler.loopSounds.clear();
                        biomeHandler.previousBiome = null;
                    });
            }
        });
    }

    private void handleWeather(ClientLevel level, Random random) {
        switch (weatherPhase) {
            case FREEZING -> handleFreezingPhase(level, random);
            case SCORCHING -> handleScorchingPhase(level, random);
        }

        weatherPhaseTicks++;
    }

    private void handleFreezingPhase(ClientLevel level, Random random) {
        float scale = 0;
        int halfSnowTicks = MirrorDimension.WeatherPhase.FREEZING.getDuration() / 2;

        if (weatherPhaseTicks >= 0 && weatherPhaseTicks < halfSnowTicks) {
            scale = (float) weatherPhaseTicks / ((float) halfSnowTicks);
        } else if (weatherPhaseTicks >= halfSnowTicks && weatherPhaseTicks < MirrorDimension.WeatherPhase.FREEZING.getDuration()) {
            scale = 1.0F - (float) (weatherPhaseTicks - halfSnowTicks) / halfSnowTicks;
        }

        int r = (MirrorDimension.FOG >> 16 & 0xFF);
        int g = (MirrorDimension.FOG >> 8 & 0xFF);
        int b = (MirrorDimension.FOG & 0xFF);

        r += 4 * scale;
        g += 10 * scale;
        b += 50 * scale;

        int newFog = 0xFF000000 | r << 16 | g << 8 | b;
        Dimensions.FOG_COLOR.put(MirrorDimension.ID, newFog);
    }

    private void handleScorchingPhase(ClientLevel level, Random random) {
        float scale = 0;
        int halfScorchTicks = MirrorDimension.WeatherPhase.SCORCHING.getDuration() / 2;

        if (weatherPhaseTicks >= 0 && weatherPhaseTicks < halfScorchTicks) {
            scale = (float) weatherPhaseTicks / ((float) halfScorchTicks);
        } else if (weatherPhaseTicks >= halfScorchTicks && weatherPhaseTicks < MirrorDimension.WeatherPhase.FREEZING.getDuration()) {
            scale = 1.0F - (float) (weatherPhaseTicks - halfScorchTicks) / halfScorchTicks;
        }

        int r = (MirrorDimension.FOG >> 16 & 0xFF);
        int g = (MirrorDimension.FOG >> 8 & 0xFF);
        int b = (MirrorDimension.FOG & 0xFF);

        r += 130 * scale;
        g += 20 * scale;
        b -= 4 * scale;

        int newFog = 0xFF000000 | r << 16 | g << 8 | b;
        Dimensions.FOG_COLOR.put(MirrorDimension.ID, newFog);
    }

    private void handleParticles(Random random) {
        if (weatherPhase == MirrorDimension.WeatherPhase.FREEZING || weatherPhase == MirrorDimension.WeatherPhase.SCORCHING) {
            if (!Dimensions.AMBIENT_PARTICLE.containsKey(MirrorDimension.ID)) {
                Dimensions.AMBIENT_PARTICLE.put(MirrorDimension.ID, MirrorDimension.ASH);
            }
        } else if (particleTicks++ > startParticlesAfter) {
            if (!Dimensions.AMBIENT_PARTICLE.containsKey(MirrorDimension.ID)) {
                Dimensions.AMBIENT_PARTICLE.put(MirrorDimension.ID, MirrorDimension.AMBIENT_PARTICLES.get(random.nextInt(MirrorDimension.AMBIENT_PARTICLES.size())));
            }

            if (particleTicks > startParticlesAfter + maxParticleTicks) {
                Dimensions.AMBIENT_PARTICLE.remove(MirrorDimension.ID);
                particleTicks = 0;
            }
        }
    }
}
