package svenhjol.strange.base.client;

import net.minecraft.client.audio.TickableSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CaveAmbientSounds
{
    public static class SubSound extends TickableSound
    {
        private final ClientPlayerEntity player;

        protected SubSound(ClientPlayerEntity player, SoundEvent sound)
        {
            super(sound, SoundCategory.AMBIENT);
            this.player = player;
            this.repeat = false;
            this.repeatDelay = 0;
            this.volume = 1.0F;
            this.priority = true;
            this.global = true;
        }

        @Override
        public void tick()
        {
            if (this.player.removed) {
                this.donePlaying = true;
            }
        }
    }

    public static class CaveSound extends TickableSound
    {
        private final ClientPlayerEntity player;
        private int ticksUnderground;

        public CaveSound(ClientPlayerEntity player)
        {
            super(SoundEvents.AMBIENT_CAVE, SoundCategory.AMBIENT);
            this.player = player;
            this.repeat = false;
            this.repeatDelay = 0;
            this.volume = 1.0F;
            this.priority = true;
            this.global = true;
        }

        @Override
        public void tick()
        {
            if (!this.player.removed) {
                World world = this.player.world;
                BlockPos pos = this.player.getPosition();
                if (!world.isSkyLightMax(pos) && pos.getY() < 60) {
                    ++this.ticksUnderground;
                } else {
                    this.ticksUnderground -= 2;
                }

                this.ticksUnderground = Math.min(this.ticksUnderground, 40);
                this.volume = Math.max(0.0F, Math.min((float)this.ticksUnderground / 40.0F, 1.0F));
            } else {
                this.donePlaying = true;
            }
        }
    }
}
