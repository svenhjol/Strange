package svenhjol.strange.ambience.client.iface;

import net.minecraft.util.SoundEvent;
import svenhjol.strange.ambience.client.LongSound;
import svenhjol.strange.ambience.client.ShortSound;

import javax.annotation.Nullable;

public interface IDaySounds extends IAmbientSounds
{
    @Override
    default void playLongSounds()
    {
        if (hasLongSound()) {
            getSoundHandler().play(new LongSound(getPlayer(), getLongSound(), getLongSoundVolume(), p -> isValid()));
        }
    }

    @Override
    default void playShortSounds()
    {
        if (hasShortSound()) {
            getSoundHandler().play(new ShortSound(getPlayer(), getShortSound(), getLongSoundVolume()));
        }
    }

    @Nullable
    SoundEvent getLongSound();

    @Nullable
    SoundEvent getShortSound();

    @Override
    default boolean hasLongSound()
    {
        return getLongSound() != null;
    }

    @Override
    default boolean hasShortSound()
    {
        return getShortSound() != null;
    }
}
