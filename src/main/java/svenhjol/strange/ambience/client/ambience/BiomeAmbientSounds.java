package svenhjol.strange.ambience.client.ambience;

import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.BiomeManager;

import java.util.Random;

public abstract class BiomeAmbientSounds extends BaseAmbientSounds
{
    protected SoundEvent shortSound;
    protected SoundEvent longSound;
    protected BiomeManager.BiomeType biomeType;

    public BiomeAmbientSounds(ClientPlayerEntity player, SoundHandler soundHandler)
    {
        super(player, soundHandler);
    }

    private int shortDelay = 0;
    private boolean isInBiome = false;

    @Override
    public void tick()
    {
        Random rand = world.rand;
        boolean nowInBiome = isValidPos();

        if (!nowInBiome)
            isInBiome = false;

        if (!isInBiome && nowInBiome) {
            soundHandler.play(new LongSound(player, this.longSound, 0.4F));
            isInBiome = true;
        }

        if (nowInBiome && --shortDelay <= 0) {
            soundHandler.play(new ShortSound(player, this.shortSound, 0.44F));
            shortDelay = rand.nextInt(400) + 1000;
        }
    }
}
