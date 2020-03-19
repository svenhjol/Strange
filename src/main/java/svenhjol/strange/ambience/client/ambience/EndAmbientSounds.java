package svenhjol.strange.ambience.client.ambience;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.dimension.DimensionType;
import svenhjol.strange.base.StrangeSounds;

import javax.annotation.Nullable;

public class EndAmbientSounds extends BaseAmbientSounds {
    public EndAmbientSounds(PlayerEntity player, SoundHandler soundHandler) {
        super(player, soundHandler);
    }

    public boolean isValid() {
        if (world == null) return false;
        return world.getDimension().getType() == DimensionType.THE_END;
    }

    @Override
    public int getShortSoundDelay() {
        return world.rand.nextInt(200) + 200;
    }

    @Nullable
    @Override
    public SoundEvent getLongSound() {
        return StrangeSounds.AMBIENCE_END_LONG;
    }

    @Nullable
    @Override
    public SoundEvent getShortSound() {
        return null;
    }
}
