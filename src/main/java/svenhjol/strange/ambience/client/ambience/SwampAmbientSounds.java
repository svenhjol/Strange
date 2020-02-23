package svenhjol.strange.ambience.client.ambience;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.biome.Biome;
import svenhjol.strange.ambience.client.iface.IBiomeAmbience;
import svenhjol.strange.base.StrangeLoader;
import svenhjol.strange.base.StrangeSounds;

import javax.annotation.Nullable;

public class SwampAmbientSounds
{
    public static class Day extends BaseAmbientSounds implements IBiomeAmbience
    {
        public Day(PlayerEntity player, SoundHandler soundHandler)
        {
            super(player, soundHandler);
        }

        @Nullable
        @Override
        public SoundEvent getLongSound()
        {
            return StrangeSounds.AMBIENCE_SWAMP_DAY_LONG;
        }

        @Nullable
        @Override
        public SoundEvent getShortSound()
        {
            return null;
        }

        @Override
        public boolean validBiomeConditions(Biome.Category biomeCategory)
        {
            return biomeCategory == Biome.Category.SWAMP
                && isOutside()
                && StrangeLoader.client.isDaytime;
        }
    }

    public static class Night extends BaseAmbientSounds implements IBiomeAmbience
    {
        public Night(PlayerEntity player, SoundHandler soundHandler)
        {
            super(player, soundHandler);
        }

        @Nullable
        @Override
        public SoundEvent getLongSound()
        {
            return StrangeSounds.AMBIENCE_SWAMP_NIGHT_LONG;
        }

        @Nullable
        @Override
        public SoundEvent getShortSound()
        {
            return null;
        }

        @Override
        public boolean validBiomeConditions(Biome.Category biomeCategory)
        {
            return biomeCategory == Biome.Category.SWAMP
                && isOutside()
                && !StrangeLoader.client.isDaytime;
        }
    }
}
