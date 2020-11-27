package svenhjol.strange.legendaryitems.potions;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import svenhjol.strange.legendaryitems.ILegendaryPotion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LegendaryPotion implements ILegendaryPotion {
    @Override
    public List<StatusEffect> getValidStatusEffects() {
        List<StatusEffect> possibleEffects = new ArrayList<>(Arrays.asList(
            StatusEffects.SPEED,
            StatusEffects.HASTE,
            StatusEffects.STRENGTH,
            StatusEffects.JUMP_BOOST,
            StatusEffects.RESISTANCE,
            StatusEffects.HEALTH_BOOST,
            StatusEffects.ABSORPTION,
            StatusEffects.SATURATION,
            StatusEffects.LUCK,
            StatusEffects.DOLPHINS_GRACE
        ));

        List<StatusEffect> selectedEffects = new ArrayList<>();
        selectedEffects.add(possibleEffects.get(new Random().nextInt(possibleEffects.size())));
        return selectedEffects;
    }

    @Override
    public int getMinDuration() {
        return 480;
    }

    @Override
    public int getMaxDuration() {
        return 960;
    }

    @Override
    public int getMinAmplifier() {
        return 3;
    }

    @Override
    public int getMaxAmplifier() {
        return 5;
    }
}