package svenhjol.strange.ambience.client.ambience;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import svenhjol.strange.base.StrangeSounds;

import java.util.Random;

public class DeepAmbientSounds extends BaseAmbientSounds
{
    public DeepAmbientSounds(ClientPlayerEntity player, SoundHandler soundHandler)
    {
        super(player, soundHandler);
    }

    private int shortDelay = 0;
    private boolean isInDeep = false;

    @Override
    public boolean isValidPos()
    {
        if (world == null) return false;
        BlockPos pos = player.getPosition();
        int light = world.getLight(pos);
        return !world.isSkyLightMax(pos) && pos.getY() <= 32 && light < 10;
    }

    @Override
    public void tick()
    {
        Random rand = world.rand;
        boolean nowInDeep = isValidPos();

        if (!nowInDeep)
            isInDeep = false;

        if (!isInDeep && nowInDeep) {
            soundHandler.play(new DeepAmbientSounds.LongSound(player, StrangeSounds.AMBIENCE_DEEP_LONG, 0.4F));
            isInDeep = true;
        }

        if (nowInDeep && --shortDelay <= 0) {
            soundHandler.play(new DeepAmbientSounds.ShortSound(player, StrangeSounds.AMBIENCE_DEEP_SHORT, 0.5F));
            shortDelay = rand.nextInt(400) + 1000;
        }
    }
}
