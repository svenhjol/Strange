package svenhjol.strange.ambience.client.iface;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nullable;

public interface IAmbientSounds
{
    ClientWorld getWorld();

    ClientPlayerEntity getPlayer();

    SoundHandler getSoundHandler();

    boolean isValid();

    @Nullable
    SoundEvent getLongSound();

    @Nullable
    SoundEvent getShortSound();
}
