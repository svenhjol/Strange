package svenhjol.strange.ambience.client.ambience;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundEvent;
import svenhjol.strange.ambience.client.AmbienceHandler;
import svenhjol.strange.ambience.client.iface.IDaySounds;
import svenhjol.strange.base.StrangeSounds;

import javax.annotation.Nullable;

public class FortressAmbientSounds extends BaseAmbientSounds implements IDaySounds
{
    public FortressAmbientSounds(ClientPlayerEntity player, SoundHandler soundHandler)
    {
        super(player, soundHandler);
    }

    @Override
    public boolean isValid()
    {
        if (world == null) return false;
        return AmbienceHandler.isInFortress;
    }

    @Override
    public int getShortSoundDelay()
    {
        return world.rand.nextInt(400) + 600;
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
        return StrangeSounds.AMBIENCE_FORTRESS_SHORT;
    }
}
