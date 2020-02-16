package svenhjol.strange.ambience.client.ambience;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.world.dimension.DimensionType;
import svenhjol.strange.base.StrangeSounds;

import java.util.Random;

public class NetherAmbientSounds extends BaseAmbientSounds
{
    public NetherAmbientSounds(ClientPlayerEntity player, SoundHandler soundHandler)
    {
        super(player, soundHandler);
    }

    private int shortDelay = 0;
    private boolean isInNether = false;

    @Override
    public boolean isValidPos()
    {
        if (world == null) return false;
        return world.getDimension().getType() == DimensionType.THE_NETHER;
    }

    @Override
    public void tick()
    {
        Random rand = world.rand;
        boolean nowInNether = isValidPos();

        if (!nowInNether)
            isInNether = false;

        if (!isInNether && nowInNether) {
            soundHandler.play(new LongSound(player, StrangeSounds.AMBIENCE_NETHER_LONG, 0.8F));
            isInNether = true;
        }

        if (nowInNether && --shortDelay <= 0) {
            soundHandler.play(new ShortSound(player, StrangeSounds.AMBIENCE_NETHER_SHORT, 0.5F));
            shortDelay = rand.nextInt(400) + 800;
        }
    }
}
