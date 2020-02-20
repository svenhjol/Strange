package svenhjol.strange.ambience.client.ambience;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.dimension.DimensionType;
import svenhjol.strange.base.StrangeSounds;

import javax.annotation.Nullable;

public class HighAmbientSounds extends BaseAmbientSounds
{
    public HighAmbientSounds(ClientPlayerEntity player, SoundHandler soundHandler)
    {
        super(player, soundHandler);
    }

    @Override
    public boolean isValid()
    {
        return world.dimension.getType() == DimensionType.OVERWORLD
            && player.getPosition().getY() > 150;
    }

    @Nullable
    @Override
    public SoundEvent getLongSound()
    {
        return StrangeSounds.AMBIENCE_HIGH;
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
        return 1000;
    }

    @Override
    public float getLongSoundVolume()
    {
        return 0.05F;
    }
}
