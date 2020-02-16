package svenhjol.strange.ambience.client;

import net.minecraft.client.audio.IAmbientSoundHandler;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.strange.ambience.client.ambience.*;
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
    public static boolean isInFortress = false;
    public static boolean isInShipwreck = false;
    public static boolean isInVillage = false;
    public static boolean isInUndergroundRuin = false;
    public static boolean isInVaults = false;
    public static boolean isInBigDungeon = false;

    public static void updateStructures(CompoundNBT structures)
    {
        isInMineshaft = structures.getBoolean("mineshaft");
        isInStronghold = structures.getBoolean("stronghold");
        isInFortress = structures.getBoolean("fortress");
        isInShipwreck = structures.getBoolean("shipwreck");
        isInVillage = structures.getBoolean("village");
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
            new FortressAmbientSounds(player, soundHandler),
            new MineshaftAmbientSounds(player, soundHandler),
            new NetherAmbientSounds(player, soundHandler)
        ));
    }

    @Override
    public void tick()
    {
        if (!player.isAlive()) return;
        if (player.world == null) return;

        if (player.world.getGameTime() % 200 == 0)
            PacketHandler.sendToServer(new ServerUpdateStructures());

        ambientSounds.forEach(BaseAmbientSounds::tick);
    }
}
