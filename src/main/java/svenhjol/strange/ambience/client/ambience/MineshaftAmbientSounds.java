package svenhjol.strange.ambience.client.ambience;

import net.minecraft.block.material.Material;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import svenhjol.strange.base.StrangeSounds;

public class MineshaftAmbientSounds extends BaseAmbientSounds
{
    private int shortDelay = 0;

    public MineshaftAmbientSounds(ClientPlayerEntity player, SoundHandler soundHandler)
    {
        super(player, soundHandler);
    }

    @Override
    public boolean isValidPos()
    {
        if (world == null) return false;
        BlockPos pos = player.getPosition().down();
        return world.getBlockState(pos).getMaterial() == Material.WOOD;
    }

    @Override
    public void tick()
    {
        if (isValidPos() && --shortDelay <= 0) {
            soundHandler.play(new ShortSound(player, StrangeSounds.AMBIENCE_MINESHAFT_SHORT, 0.5F));
            shortDelay = world.rand.nextInt(100) + 60;
        }
    }
}
