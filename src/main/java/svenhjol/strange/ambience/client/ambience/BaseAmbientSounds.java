package svenhjol.strange.ambience.client.ambience;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import svenhjol.strange.ambience.client.LongSound;
import svenhjol.strange.ambience.client.ShortSound;
import svenhjol.strange.ambience.client.iface.IAmbientSounds;

public abstract class BaseAmbientSounds implements IAmbientSounds
{
    protected int shortTicks = 0;
    protected boolean isValid = false;
    protected TickableSound longSound = null;

    protected ClientPlayerEntity player;
    protected ClientWorld world;
    protected SoundHandler soundHandler;

    public BaseAmbientSounds(ClientPlayerEntity player, SoundHandler soundHandler)
    {
        this.player = player;
        this.soundHandler = soundHandler;
        this.world = (ClientWorld)player.world;
    }

    public void tick()
    {
        boolean nowValid = isValid();

        if (isValid && !nowValid) {
            isValid = false;
        }

        if (!isValid && nowValid) {
            isValid = true;
            shortTicks = getShortSoundDelay();
        }

        if (nowValid && hasShortSound() && --shortTicks <= 0) {
            setShortSound();
            shortTicks = getShortSoundDelay();
        }

        if (isValid && hasLongSound() && !isPlayingLongSound()) {
            setLongSound();
            soundHandler.play(this.longSound);
        }
    }

    public boolean isOutside()
    {
        if (player.canSwim()) return false;

        int blocks = 16;
        int start = 1;

        BlockPos playerPos = player.getPosition();

        for (int i = start; i < start + blocks; i++) {
            BlockPos check = new BlockPos(playerPos.getX(), playerPos.getY() + i, playerPos.getZ());
            BlockState state = world.getBlockState(check);
            Block block = state.getBlock();

            if (world.canBlockSeeSky(check)) return true;

            if (world.isAirBlock(check)) continue;
            if (state.getMaterial() == Material.GLASS
                || block instanceof LogBlock
                || block instanceof MushroomBlock
                || block instanceof HugeMushroomBlock
            ) continue;

            if (state.isSolid()) return false;
        }

        if (player.getPosition().getY() < 48) return false;

        return true;
    }

    protected void setShortSound()
    {
        soundHandler.play(new ShortSound(player, getShortSound(), getShortSoundVolume()));
    }

    protected void setLongSound()
    {
        this.longSound = new LongSound(player, getLongSound(), getLongSoundVolume(), p -> isValid());
    }

    public boolean isPlayingLongSound()
    {
        return this.longSound != null && !this.longSound.isDonePlaying();
    }

    public float getShortSoundVolume()
    {
        return 0.65F;
    }

    public float getLongSoundVolume()
    {
        return 0.5F;
    }

    public int getShortSoundDelay()
    {
        return world.rand.nextInt(400) + 400;
    }

    public boolean hasLongSound()
    {
        return getLongSound() != null;
    }

    public boolean hasShortSound()
    {
        return getShortSound() != null;
    }

    @Override
    public ClientWorld getWorld()
    {
        return world;
    }

    @Override
    public ClientPlayerEntity getPlayer()
    {
        return player;
    }

    @Override
    public SoundHandler getSoundHandler()
    {
        return soundHandler;
    }
}
