package svenhjol.strange.ambience.client;

import net.minecraft.client.audio.IAmbientSoundHandler;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import svenhjol.strange.ambience.client.ambience.BaseAmbientSounds;
import svenhjol.strange.ambience.client.ambience.CaveAmbientSounds;
import svenhjol.strange.ambience.client.ambience.DeepAmbientSounds;
import svenhjol.strange.ambience.client.ambience.MineshaftAmbientSounds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AmbienceHandler implements IAmbientSoundHandler
{
    private final ClientPlayerEntity player;
    private final SoundHandler soundHandler;
    private List<BaseAmbientSounds> ambientSounds = new ArrayList<>();

    public AmbienceHandler(ClientPlayerEntity player, SoundHandler soundHandler)
    {
        this.player = player;
        this.soundHandler = soundHandler;

        ambientSounds.addAll(Arrays.asList(
            new CaveAmbientSounds(player, soundHandler),
            new DeepAmbientSounds(player, soundHandler),
            new MineshaftAmbientSounds(player, soundHandler)
        ));

    }

    @Override
    public void tick()
    {
        if (!this.player.isAlive()) return;
        if (this.player.world == null) return;

        ambientSounds.forEach(BaseAmbientSounds::tick);
    }
}
