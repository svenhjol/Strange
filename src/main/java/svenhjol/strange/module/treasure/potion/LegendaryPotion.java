package svenhjol.strange.module.treasure.potion;

import svenhjol.strange.module.treasure.ITreasurePotion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

public class LegendaryPotion implements ITreasurePotion {
    @Override
    public List<MobEffect> getValidStatusEffects() {
        List<MobEffect> possibleEffects = new ArrayList<>(Arrays.asList(
            MobEffects.MOVEMENT_SPEED,
            MobEffects.DIG_SPEED,
            MobEffects.DAMAGE_BOOST,
            MobEffects.JUMP,
            MobEffects.DAMAGE_RESISTANCE,
            MobEffects.HEALTH_BOOST,
            MobEffects.ABSORPTION,
            MobEffects.SATURATION,
            MobEffects.LUCK,
            MobEffects.DOLPHINS_GRACE
        ));

        List<MobEffect> selectedEffects = new ArrayList<>();
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