package svenhjol.strange.ambience.client.ambience;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import svenhjol.strange.base.StrangeSounds;

import javax.annotation.Nullable;

public class CaveAmbientSounds extends BaseAmbientSounds
{
    public CaveAmbientSounds(ClientPlayerEntity player, SoundHandler soundHandler)
    {
        super(player, soundHandler);
    }

    @Override
    public boolean isValid()
    {
        if (world == null || world.getDimension().getType() != DimensionType.OVERWORLD) return false;
        BlockPos pos = player.getPosition();
        int light = world.getLight(pos);

        if (!world.isSkyLightMax(pos) && pos.getY() < world.getSeaLevel()) {
            return pos.getY() <= 44 || light <= 13;
        }

        return false;
    }

    @Override
    public int getShortSoundDelay()
    {
        return world.rand.nextInt(300) + 600;
    }

    @Nullable
    @Override
    public SoundEvent getLongSound()
    {
        return StrangeSounds.AMBIENCE_CAVE_LONG;
    }

    @Nullable
    @Override
    public SoundEvent getShortSound()
    {
        return StrangeSounds.AMBIENCE_CAVE_SHORT;
    }
}
