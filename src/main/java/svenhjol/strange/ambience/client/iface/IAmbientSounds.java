package svenhjol.strange.ambience.client.iface;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;

public interface IAmbientSounds
{
    ClientWorld getWorld();

    ClientPlayerEntity getPlayer();

    SoundHandler getSoundHandler();

    boolean isValid();

    boolean hasLongSound();

    boolean hasShortSound();

    float getLongSoundVolume();

    float getShortSoundVolume();

    int getShortSoundDelay();

    void playLongSounds();

    void playShortSounds();
}
