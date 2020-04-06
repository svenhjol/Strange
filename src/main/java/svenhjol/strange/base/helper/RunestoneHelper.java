package svenhjol.strange.base.helper;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RunestoneHelper {
    private static final Map<Character, Character> runeHexCharMap = new HashMap<>();
    private static final Map<Integer, Character> runeIntCharMap = new HashMap<>();

    public static Map<Character, Character> getRuneHexCharMap() {
        return runeHexCharMap;
    }

    public static Map<Integer, Character> getRuneIntCharMap() {
        return runeIntCharMap;
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
        runeHexCharMap.put('0', 'a');
        runeHexCharMap.put('1', 'b');
        runeHexCharMap.put('2', 'd');
        runeHexCharMap.put('3', 'e');
        runeHexCharMap.put('4', 'q');
        runeHexCharMap.put('5', 't');
        runeHexCharMap.put('6', 'g');
        runeHexCharMap.put('7', 'h');
        runeHexCharMap.put('8', 'i');
        runeHexCharMap.put('9', 'p');
        runeHexCharMap.put('a', 'v');
        runeHexCharMap.put('b', 'o');
        runeHexCharMap.put('c', 'z');
        runeHexCharMap.put('d', 'j');
        runeHexCharMap.put('e', 'w');
        runeHexCharMap.put('f', 'f');

        runeIntCharMap.put(0, 'a');
        runeIntCharMap.put(1, 'b');
        runeIntCharMap.put(2, 'd');
        runeIntCharMap.put(3, 'e');
        runeIntCharMap.put(4, 'q');
        runeIntCharMap.put(5, 't');
        runeIntCharMap.put(6, 'g');
        runeIntCharMap.put(7, 'h');
        runeIntCharMap.put(8, 'i');
        runeIntCharMap.put(9, 'p');
        runeIntCharMap.put(10, 'v');
        runeIntCharMap.put(11, 'o');
        runeIntCharMap.put(12, 'z');
        runeIntCharMap.put(13, 'j');
        runeIntCharMap.put(14, 'w');
        runeIntCharMap.put(15, 'f');
    }
}
