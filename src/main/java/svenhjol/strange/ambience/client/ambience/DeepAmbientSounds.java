package svenhjol.strange.ambience.client.ambience;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import svenhjol.strange.base.StrangeSounds;

import javax.annotation.Nullable;

public class DeepAmbientSounds extends BaseAmbientSounds
{
    public DeepAmbientSounds(ClientPlayerEntity player, SoundHandler soundHandler)
    {
        super(player, soundHandler);
    }

    @Override
    public boolean isValid()
    {
        if (world == null || world.getDimension().getType() != DimensionType.OVERWORLD) return false;
        BlockPos pos = player.getPosition();
        int light = world.getLight(pos);
        return !world.isSkyLightMax(pos) && pos.getY() <= 32 && light < 10;
    }

    @Override
    public int getShortSoundDelay()
    {
        return world.rand.nextInt(400) + 1000;
    }

    @Nullable
    @Override
    public SoundEvent getLongSound()
    {
        return StrangeSounds.AMBIENCE_DEEP_LONG;
    }

    @Nullable
    @Override
    public SoundEvent getShortSound()
    {
        return StrangeSounds.AMBIENCE_DEEP_SHORT;
    }
}
