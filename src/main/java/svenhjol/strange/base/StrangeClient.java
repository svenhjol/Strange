package svenhjol.strange.base;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StrangeClient
{
    public boolean isInMineshaft = false;
    public boolean isInStronghold = false;
    public boolean isInFortress = false;
    public boolean isInShipwreck = false;
    public boolean isInVillage = false;
    public boolean isInUndergroundRuin = false;
    public boolean isInVaults = false;
    public boolean isInBigDungeon = false;
    public boolean isDaytime = true;

    public void updateStructures(CompoundNBT structures)
    {
        isInMineshaft = structures.getBoolean("mineshaft");
        isInStronghold = structures.getBoolean("stronghold");
        isInFortress = structures.getBoolean("fortress");
        isInShipwreck = structures.getBoolean("shipwreck");
        isInVillage = structures.getBoolean("village");
        isInBigDungeon = structures.getBoolean("big_dungeon");
        isInUndergroundRuin = structures.getBoolean("underground_ruin");
        isInVaults = structures.getBoolean("vaults");
        isDaytime = structures.getBoolean("day");
    }
}
