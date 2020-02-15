package svenhjol.strange.ambience.client;

import net.minecraft.client.audio.IAmbientSoundHandler;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.strange.ambience.client.ambience.BaseAmbientSounds;
import svenhjol.strange.ambience.client.ambience.CaveAmbientSounds;
import svenhjol.strange.ambience.client.ambience.DeepAmbientSounds;
import svenhjol.strange.ambience.client.ambience.MineshaftAmbientSounds;
import svenhjol.strange.ambience.message.ServerUpdateStructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AmbienceHandler implements IAmbientSoundHandler
{
    private final ClientPlayerEntity player;
    private List<BaseAmbientSounds> ambientSounds = new ArrayList<>();

    public static boolean isInMineshaft = false;
    public static boolean isInStronghold = false;
    public static boolean isInNetherFortress = false;
    public static boolean isInUndergroundRuin = false;
    public static boolean isInVaults = false;
    public static boolean isInBigDungeon = false;

    public static void updateStructures(CompoundNBT structures)
    {
        isInMineshaft = structures.getBoolean("mineshaft");
        isInStronghold = structures.getBoolean("stronghold");
        isInNetherFortress = structures.getBoolean("fortress");
        isInBigDungeon = structures.getBoolean("big_dungeon");
        isInUndergroundRuin = structures.getBoolean("underground_ruin");
        isInVaults = structures.getBoolean("vaults");
    }

    public AmbienceHandler(ClientPlayerEntity player, SoundHandler soundHandler)
    {
        this.player = player;

        ambientSounds.addAll(Arrays.asList(
            new CaveAmbientSounds(player, soundHandler),
            new DeepAmbientSounds(player, soundHandler),
            new MineshaftAmbientSounds(player, soundHandler)
        ));
    }

    @Override
    public void tick()
    {
        if (!player.isAlive()) return;
        if (player.world == null) return;

        if (player.world.getGameTime() % 160 == 0)
            PacketHandler.sendToServer(new ServerUpdateStructures());

        ambientSounds.forEach(BaseAmbientSounds::tick);
    }
}
