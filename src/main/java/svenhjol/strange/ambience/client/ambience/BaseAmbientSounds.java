package svenhjol.strange.ambience.client.ambience;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import svenhjol.strange.ambience.client.iface.IAmbientSounds;

public abstract class BaseAmbientSounds implements IAmbientSounds
{
    private int shortTicks = 0;
    private boolean isValid = false;

    protected ClientPlayerEntity player;
    protected ClientWorld world;
    protected SoundHandler soundHandler;

    public BaseAmbientSounds(ClientPlayerEntity player, SoundHandler soundHandler)
    {
        this.player = player;
        this.soundHandler = soundHandler;
        this.world = (ClientWorld)player.world;
    }

    @Override
    public SoundHandler getSoundHandler()
    {
        return soundHandler;
    }

    @Override
    public ClientPlayerEntity getPlayer()
    {
        return player;
    }

    @Override
    public ClientWorld getWorld()
    {
        return world;
    }

    public void tick()
    {
        boolean nowValid = isValid();

        if (!nowValid)
            isValid = false;

        if (!isValid && nowValid) {
            playLongSounds();
            isValid = true;
        }

        if (nowValid && --shortTicks <= 0) {
            playShortSounds();
            shortTicks = getShortSoundDelay();
        }
    }

    @Override
    public float getShortSoundVolume()
    {
        return 0.5F;
    }

    @Override
    public float getLongSoundVolume()
    {
        return 0.4F;
    }
}
