package svenhjol.strange.legendary.potions;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import svenhjol.strange.iface.ILegendaryPotion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Luck implements ILegendaryPotion {

    @Override
    public List<StatusEffect> getValidStatusEffects() {
        return new ArrayList<>(Arrays.asList(StatusEffects.LUCK));
    }

    @Override
    public int getMinDuration() {
        return 60;
    }

    @Override
    public int getMaxDuration() {
        return 120;
    }

    @Override
    public int getMinAmplifier() {
        return 2;
    }

    @Override
    public int getMaxAmplifier() {
        return 4;
    }
}