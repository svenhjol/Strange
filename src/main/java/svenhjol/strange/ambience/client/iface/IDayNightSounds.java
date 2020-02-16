package svenhjol.strange.ambience.client.iface;

import net.minecraft.util.SoundEvent;
import svenhjol.strange.ambience.client.LongSound;
import svenhjol.strange.ambience.client.ShortSound;

import javax.annotation.Nullable;

public interface IDayNightSounds extends IAmbientSounds
{
    default boolean isDay()
    {
        return isValid() && getWorld().isDaytime();
    }

    default boolean isNight()
    {
        return isValid() && !getWorld().isDaytime();
    }

    @Override
    default void playLongSounds()
    {
        if (!hasLongSound()) return;

        if (isDay())
            getSoundHandler().play(new LongSound(getPlayer(), getDayLongSound(), getLongSoundVolume(), p -> isDay()));

        if (isNight())
            getSoundHandler().play(new LongSound(getPlayer(), getNightLongSound(), getLongSoundVolume(), p -> isNight()));
    }

    @Override
    default void playShortSounds()
    {
        if (!hasShortSound()) return;

        if (isDay())
            getSoundHandler().play(new ShortSound(getPlayer(), getDayShortSound(), getShortSoundVolume()));

        if (isNight())
            getSoundHandler().play(new ShortSound(getPlayer(), getNightShortSound(), getShortSoundVolume()));
    }

    @Nullable
    SoundEvent getDayShortSound();

    @Nullable
    SoundEvent getDayLongSound();

    @Nullable
    SoundEvent getNightShortSound();

    @Nullable
    SoundEvent getNightLongSound();

    @Override
    default boolean hasShortSound()
    {
        if (isDay())
            return getDayShortSound() != null;

        if (isNight())
            return getNightShortSound() != null;

        return false;
    }

    @Override
    default boolean hasLongSound()
    {
        if (isDay())
            return getDayLongSound() != null;

        if (isNight())
            return getNightLongSound() != null;

        return false;
    }
}
