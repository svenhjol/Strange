package svenhjol.strange.ambience.client.ambience;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.biome.Biome;
import svenhjol.strange.ambience.client.iface.IBiomeAmbience;
import svenhjol.strange.base.StrangeLoader;
import svenhjol.strange.base.StrangeSounds;

import javax.annotation.Nullable;

public class PlainsAmbientSounds
{
    public static class Day extends BaseAmbientSounds implements IBiomeAmbience
    {
        public Day(ClientPlayerEntity player, SoundHandler soundHandler)
        {
            super(player, soundHandler);
        }

        @Nullable
        @Override
        public SoundEvent getLongSound()
        {
            return StrangeSounds.AMBIENCE_PLAINS_DAY_LONG;
        }

        @Nullable
        @Override
        public SoundEvent getShortSound()
        {
            return null;
        }

        @Override
        public int getShortSoundDelay()
        {
            return world.rand.nextInt(400) + 400;
        }

        @Override
        public float getLongSoundVolume()
        {
            return 0.08F;
        }

        @Override
        public boolean validBiomeConditions(Biome.Category biomeCategory)
        {
            return biomeCategory == Biome.Category.PLAINS
                || biomeCategory == Biome.Category.MUSHROOM
                && isOutside()
                && StrangeLoader.client.isDaytime;
        }
    }

    public static class Night extends BaseAmbientSounds implements IBiomeAmbience
    {
        public Night(ClientPlayerEntity player, SoundHandler soundHandler)
        {
            super(player, soundHandler);
        }

        @Nullable
        @Override
        public SoundEvent getLongSound()
        {
            return StrangeSounds.AMBIENCE_PLAINS_NIGHT_LONG;
        }

        @Nullable
        @Override
        public SoundEvent getShortSound()
        {
            return StrangeSounds.AMBIENCE_PLAINS_NIGHT_SHORT;
        }

        @Override
        public int getShortSoundDelay()
        {
            return world.rand.nextInt(500) + 200;
        }

        @Override
        public float getLongSoundVolume()
        {
            return 0.08F;
        }

        @Override
        public boolean validBiomeConditions(Biome.Category biomeCategory)
        {
            return biomeCategory == Biome.Category.PLAINS
                || biomeCategory == Biome.Category.MUSHROOM
                && isOutside()
                && !StrangeLoader.client.isDaytime;
        }
    }
}
