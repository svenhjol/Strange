package svenhjol.strange.ambience.client.ambience;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.dimension.DimensionType;
import svenhjol.strange.base.StrangeSounds;

import javax.annotation.Nullable;

public class NetherAmbientSounds extends BaseAmbientSounds
{
    public NetherAmbientSounds(ClientPlayerEntity player, SoundHandler soundHandler)
    {
        super(player, soundHandler);
    }

    public boolean isValid()
    {
        if (world == null) return false;
        return world.getDimension().getType() == DimensionType.THE_NETHER;
    }

    @Override
    public int getShortSoundDelay()
    {
        return world.rand.nextInt(200) + 200;
    }

    @Nullable
    @Override
    public SoundEvent getLongSound()
    {
        return StrangeSounds.AMBIENCE_NETHER_LONG;
    }

    @Nullable
    @Override
    public SoundEvent getShortSound()
    {
        return StrangeSounds.AMBIENCE_NETHER_SHORT;
    }
}
