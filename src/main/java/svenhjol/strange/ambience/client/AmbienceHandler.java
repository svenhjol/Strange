package svenhjol.strange.ambience.client;

import net.minecraft.client.audio.IAmbientSoundHandler;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import svenhjol.strange.ambience.client.ambience.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AmbienceHandler implements IAmbientSoundHandler
{
    private final ClientPlayerEntity player;
    private List<BaseAmbientSounds> ambientSounds = new ArrayList<>();

    public AmbienceHandler(ClientPlayerEntity player, SoundHandler soundHandler)
    {
        this.player = player;

        ambientSounds.addAll(Arrays.asList(
            new CaveAmbientSounds(player, soundHandler),
            new DeepAmbientSounds(player, soundHandler),
            new DesertAmbientSounds.Day(player, soundHandler),
            new DesertAmbientSounds.Night(player, soundHandler),
            new MineshaftAmbientSounds(player, soundHandler),
            new NetherAmbientSounds(player, soundHandler)
        ));
    }

    @Override
    public void tick()
    {
        if (!player.isAlive() || player.world == null) return;
        ambientSounds.forEach(BaseAmbientSounds::tick);
    }
}
