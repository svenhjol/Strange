package svenhjol.strange.ambience.client.ambience;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import svenhjol.strange.base.StrangeSounds;

import java.util.Random;

public class CaveAmbientSounds extends BaseAmbientSounds
{
    private int shortDelay = 0;
    private boolean isInCave = false;

    public CaveAmbientSounds(ClientPlayerEntity player, SoundHandler soundHandler)
    {
        super(player, soundHandler);
    }

    @Override
    public boolean isValidPos()
    {
        if (world == null) return false;
        BlockPos pos = player.getPosition();
        return !world.isSkyLightMax(pos) && pos.getY() <= world.getSeaLevel();
    }

    @Override
    public void tick()
    {
        Random rand = world.rand;
        boolean nowInCave = isValidPos();

        if (!nowInCave)
            isInCave = false;

        if (!isInCave && nowInCave) {
            soundHandler.play(new CaveAmbientSounds.LongSound(player, StrangeSounds.AMBIENCE_CAVE_LONG, 0.45F));
            isInCave = true;
        }

        if (nowInCave && --shortDelay <= 0) {
            soundHandler.play(new CaveAmbientSounds.ShortSound(player, StrangeSounds.AMBIENCE_CAVE_SHORT, 0.5F));
            shortDelay = rand.nextInt(300) + 600;
        }
    }
}
