package svenhjol.strange.ambience.client.ambience;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundEvent;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeSounds;

import javax.annotation.Nullable;

public class MineshaftAmbientSounds extends BaseAmbientSounds {
    public MineshaftAmbientSounds(PlayerEntity player, SoundHandler soundHandler) {
        super(player, soundHandler);
    }

    @Override
    public boolean isValid() {
        if (world == null) return false;
        return Strange.client.isInMineshaft;
    }

    @Override
    public int getShortSoundDelay() {
        return world.rand.nextInt(80) + 160;
    }

    @Nullable
    @Override
    public SoundEvent getLongSound() {
        return null;
    }

    @Nullable
    @Override
    public SoundEvent getShortSound() {
        return StrangeSounds.AMBIENCE_MINESHAFT_SHORT;
    }
}
