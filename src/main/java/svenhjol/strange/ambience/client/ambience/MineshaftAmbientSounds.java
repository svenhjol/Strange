package svenhjol.strange.ambience.client.ambience;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import svenhjol.strange.ambience.client.AmbienceHandler;
import svenhjol.strange.base.StrangeSounds;

public class MineshaftAmbientSounds extends BaseAmbientSounds
{
    private int shortDelay = 0;

    public MineshaftAmbientSounds(ClientPlayerEntity player, SoundHandler soundHandler)
    {
        super(player, soundHandler);
    }

    @Override
    public boolean isValidPos()
    {
        if (world == null) return false;
        return AmbienceHandler.isInMineshaft;
    }

    @Override
    public void tick()
    {
        if (isValidPos() && --shortDelay <= 0) {
            soundHandler.play(new ShortSound(player, StrangeSounds.AMBIENCE_MINESHAFT_SHORT, 0.5F));
            shortDelay = world.rand.nextInt(80) + 160;
        }
    }
}
