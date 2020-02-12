package svenhjol.strange.base.client;

import net.minecraft.client.audio.IAmbientSoundHandler;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;

public class CaveAmbientSoundHandler implements IAmbientSoundHandler
{
    private int delay = 0;
    private final ClientPlayerEntity player;
    private final SoundHandler handler;

    public CaveAmbientSoundHandler(ClientPlayerEntity player, SoundHandler handler)
    {
        this.player = player;
        this.handler = handler;
    }

    @Override
    public void tick()
    {
        --this.delay;
        if (this.delay <= 0) {
            float f = this.player.world.rand.nextFloat();

            if (f < 0.01F) {
                this.delay = 0;
                this.handler.play(new CaveAmbientSounds.CaveSound(this.player));
            }
        }
    }
}
