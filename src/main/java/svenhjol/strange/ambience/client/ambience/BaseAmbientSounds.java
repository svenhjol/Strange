package svenhjol.strange.ambience.client.ambience;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public abstract class BaseAmbientSounds
{
    protected ClientPlayerEntity player;
    protected SoundHandler soundHandler;
    protected World world;

    public BaseAmbientSounds(ClientPlayerEntity player, SoundHandler soundHandler)
    {
        this.player = player;
        this.soundHandler = soundHandler;
        this.world = player.world;
    }

    public abstract boolean isValidPos();

    public abstract void tick();

    public static class ShortSound extends TickableSound
    {
        private final ClientPlayerEntity player;

        protected ShortSound(ClientPlayerEntity player, SoundEvent sound, float volume)
        {
            super(sound, SoundCategory.AMBIENT);
            this.player = player;
            this.repeat = false;
            this.repeatDelay = 0;
            this.volume = volume;
            this.priority = true;
            this.global = true;
        }

        @Override
        public void tick()
        {
            if (!this.player.isAlive())
                this.donePlaying = true;
        }
    }

    public class LongSound extends TickableSound
    {
        private final ClientPlayerEntity player;
        private int ticksUnderground;

        public LongSound(ClientPlayerEntity player, SoundEvent sound, float volume)
        {
            super(sound, SoundCategory.AMBIENT);
            this.player = player;
            this.repeat = true;
            this.repeatDelay = 0;
            this.volume = volume;
            this.priority = true;
            this.global = true;
        }

        @Override
        public void tick()
        {
            if (this.player.isAlive()) {
                if (isValidPos()) {
                    ++this.ticksUnderground;
                } else {
                    this.ticksUnderground -= 1;
                }

                this.ticksUnderground = Math.min(this.ticksUnderground, 70);
                this.volume = Math.max(0.0F, Math.min((float)this.ticksUnderground / 70, 1.0F));

                if (this.volume == 0.0F) {
                    // this.donePlaying = true;
                }

            } else {
                this.donePlaying = true;
            }
        }
    }
}
