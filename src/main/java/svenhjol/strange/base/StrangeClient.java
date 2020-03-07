package svenhjol.strange.base;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import svenhjol.meson.Meson;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class StrangeClient {
    public boolean isInMineshaft = false;
    public boolean isInStronghold = false;
    public boolean isInFortress = false;
    public boolean isInShipwreck = false;
    public boolean isInVillage = false;
    public boolean isInUndergroundRuin = false;
    public boolean isInVaults = false;
    public boolean isInBigDungeon = false;
    public boolean isDaytime = true;

    public List<Integer> discoveredRunes = new ArrayList<>();

    public void updateStructures(CompoundNBT input) {
        isInMineshaft = input.getBoolean("mineshaft");
        isInStronghold = input.getBoolean("stronghold");
        isInFortress = input.getBoolean("fortress");
        isInShipwreck = input.getBoolean("shipwreck");
        isInVillage = input.getBoolean("village");
        isInBigDungeon = input.getBoolean("big_dungeon");
        isInUndergroundRuin = input.getBoolean("underground_ruin");
        isInVaults = input.getBoolean("vaults");
        isDaytime = input.getBoolean("day");
    }

    public void updateDiscoveries(CompoundNBT input) {
        if (Meson.isModuleEnabled("strange:runestones") && input.contains("discoveredRunes")) {
            discoveredRunes = new ArrayList<>();
            int[] ii = input.getIntArray("discoveredRunes");
            for (int i : ii) {
                discoveredRunes.add(i);
            }
        }
    }
}
