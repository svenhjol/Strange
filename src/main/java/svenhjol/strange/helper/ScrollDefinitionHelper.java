package svenhjol.strange.helper;

import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ScrollDefinitionHelper {
    @Nullable
    public static Identifier getEntityIdFromKey(String key, Random random) {
        key = splitOptionalRandomly(key, random);
        return Identifier.tryParse(key);
    }

    public static int getCountFromValue(String value, int fallback, int rarity, Random random, boolean scale) {
        int count;

        try {
            if (value.contains("!"))
                return Integer.parseInt(value.replace("!", ""));

            if (value.contains("-")) {
                String[] split = value.split("-");
                int min = Integer.parseInt(split[0]);
                int max = Integer.parseInt(split[1]);
                count = random.nextInt(Math.max(2, max - min)) + min;
            } else if (!value.isEmpty()) {
                count = Integer.parseInt(value);
            } else {
                count = fallback;
            }

        } catch (Exception e) {
            count = fallback;
        }

        return scale ? count * rarity : count;
    }

    public static float getChanceFromValue(String value, float fallback, int rarity, boolean scale) {
        float chance;

        try {
            if (value.contains("!"))
                return Float.parseFloat(value.replace("!", ""));

            if (!value.isEmpty()) {
                chance = Float.parseFloat(value);
            } else {
                chance = fallback;
            }

        } catch (Exception e) {
            chance = fallback;
        }

        return scale ? chance + 0.1F * rarity : chance;
    }

    public static String splitOptionalRandomly(String key, Random random) {
        if (key.contains("|")) {
            String[] split = key.split("\\|");
            key = split[random.nextInt(split.length)];
        }

        key = key.trim();
        return key;
    }

    public static List<String> splitByComma(String key) {
        List<String> split = new ArrayList<>();

        if (key.contains(",")) {
            split.addAll(Arrays.asList(key.split(",")));
        } else {
            split.add(key);
        }

        return split;
    }
}
