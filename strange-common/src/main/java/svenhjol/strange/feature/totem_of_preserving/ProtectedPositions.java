package svenhjol.strange.feature.totem_of_preserving;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores totem holder positions that should not be removed by other game mechanics.
 */
public class ProtectedPositions {
    private static final Map<ResourceLocation, List<BlockPos>> POSITIONS = new HashMap<>();

    public static void add(ResourceLocation dimension, BlockPos pos) {
        POSITIONS.computeIfAbsent(dimension, a -> new ArrayList<>()).add(pos);
    }

    public static void remove(ResourceLocation dimension, BlockPos pos) {
        if (POSITIONS.containsKey(dimension)) {
            POSITIONS.get(dimension).remove(pos);
        }
    }

    public static List<BlockPos> all(ResourceLocation dimension) {
        return POSITIONS.getOrDefault(dimension, List.of());
    }

    public static boolean isProtected(ResourceLocation dimension, BlockPos pos) {
        if (POSITIONS.containsKey(dimension)) {
            return POSITIONS.get(dimension).contains(pos);
        }
        return false;
    }
}
