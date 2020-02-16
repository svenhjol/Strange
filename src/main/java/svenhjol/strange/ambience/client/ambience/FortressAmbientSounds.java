package svenhjol.strange.ambience.client.ambience;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import svenhjol.strange.ambience.client.AmbienceHandler;
import svenhjol.strange.base.StrangeSounds;

public class FortressAmbientSounds extends BaseAmbientSounds
{
    private int shortDelay = 0;

    public FortressAmbientSounds(ClientPlayerEntity player, SoundHandler soundHandler)
    {
        super(player, soundHandler);
    }

    @Override
    public boolean isValidPos()
    {
        if (world == null) return false;
        return AmbienceHandler.isInFortress;
    }

    @Override
    public void tick()
    {
        if (isValidPos() && --shortDelay <= 0) {
            soundHandler.play(new ShortSound(player, StrangeSounds.AMBIENCE_FORTRESS_SHORT, 0.5F));
            shortDelay = world.rand.nextInt(400) + 600;
        }
    }
}
