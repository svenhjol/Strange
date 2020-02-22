package svenhjol.strange.ambience.client.ambience;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundEvent;
import svenhjol.strange.base.StrangeLoader;
import svenhjol.strange.base.StrangeSounds;

import javax.annotation.Nullable;

public class VillageAmbientSounds extends BaseAmbientSounds
{
    public VillageAmbientSounds(ClientPlayerEntity player, SoundHandler soundHandler)
    {
        super(player, soundHandler);
    }

    @Override
    public boolean isValid()
    {
        if (world == null) return false;
        return StrangeLoader.client.isInVillage
            && StrangeLoader.client.isDaytime;
    }

    @Override
    public int getShortSoundDelay()
    {
        return world.rand.nextInt(120) + 120;
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
        return StrangeSounds.AMBIENCE_VILLAGE_SHORT;
    }
}
