package svenhjol.strange.ambience.client.ambience;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundEvent;
import svenhjol.strange.base.StrangeLoader;
import svenhjol.strange.base.StrangeSounds;

import javax.annotation.Nullable;

public class MineshaftAmbientSounds extends BaseAmbientSounds
{
    public MineshaftAmbientSounds(ClientPlayerEntity player, SoundHandler soundHandler)
    {
        super(player, soundHandler);
    }

    @Override
    public boolean isValid()
    {
        if (world == null) return false;
        return StrangeLoader.client.isInMineshaft;
    }

    @Override
    public int getShortSoundDelay()
    {
        return world.rand.nextInt(80) + 160;
    }

    @Nullable
    @Override
    public SoundEvent getLongSound()
    {
        return null;
    }

    @Nullable
    @Override
    public SoundEvent getShortSound()
    {
        return StrangeSounds.AMBIENCE_MINESHAFT_SHORT;
    }
}
