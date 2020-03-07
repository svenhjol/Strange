package svenhjol.strange.base.helper;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RunestoneHelper {
    private static final Map<Character, Character> runeChars = new HashMap<>();

    public static Map<Character, Character> getRuneChars() {
        return runeChars;
    }

    public static BlockPos addRandomOffset(BlockPos pos, Random rand, int max) {
        int n = rand.nextInt(max);
        int e = rand.nextInt(max);
        int s = rand.nextInt(max);
        int w = rand.nextInt(max);
        pos = pos.north(rand.nextFloat() < 0.5F ? n : -n);
        pos = pos.east(rand.nextFloat() < 0.5F ? e : -e);
        pos = pos.south(rand.nextFloat() < 0.5F ? s : -s);
        pos = pos.west(rand.nextFloat() < 0.5F ? w : -w);
        return pos;
    }

    static {
        runeChars.put('0', 'a');
        runeChars.put('1', 'b');
        runeChars.put('2', 'd');
        runeChars.put('3', 'e');
        runeChars.put('4', 'q');
        runeChars.put('5', 't');
        runeChars.put('6', 'g');
        runeChars.put('7', 'h');
        runeChars.put('8', 'i');
        runeChars.put('9', 'p');
        runeChars.put('a', 'v');
        runeChars.put('b', 'o');
        runeChars.put('c', 'z');
        runeChars.put('d', 'j');
        runeChars.put('e', 'w');
        runeChars.put('f', 'f');
    }
}
