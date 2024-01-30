package svenhjol.strange.data;

import net.minecraft.Util;
import net.minecraft.util.RandomSource;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public interface WeightedList<T> {
    LinkedList<T> values();

    default List<T> subset(int amount, RandomSource random) {
        List<T> out = List.of();
        var values = values();
        if (values.isEmpty()) return out;

        var size = values.size();
        var shuffled = Util.toShuffledList(values.stream(), random);
        return shuffled.subList(0, Math.min(amount, size));
    }

    default List<T> subset(int amount, float weight, float nudge, RandomSource random) {
        List<T> items = new LinkedList<>();
        for (int i = 0; i < amount; i++) {
            random.nextFloat();
            getWeighted(weight, nudge, random).ifPresent(items::add);
        }
        var shuffled = Util.toShuffledList(items.stream(), random);
        return shuffled;
    }

    default List<T> subset(int min, int max, float weight, float nudge, RandomSource random) {
        var amount = (int) Math.max(min, Math.min(max, Math.ceil(max * Math.min(1.0F, weight))));
        return subset(amount, weight, nudge, random);
    }

    /**
     * Get a single element from the list at the given weight.
     * Nudge is a random mutiplier added to or subtracted from the weight.
     */
    default Optional<T> getWeighted(float weight, float nudge, RandomSource random) {
        var values = values();
        if (values.isEmpty()) return Optional.empty();

        var size = values.size();
        var weighted = size * weight;
        nudge *= random.nextFloat();
        weighted += random.nextBoolean() ? nudge : -nudge;
        var index = Math.max(0, Math.min(size - 1, Math.round(weighted)));

        return Optional.of(values.get(index));
    }

    default Optional<T> getRandom(RandomSource random) {
        var values = values();
        if (values.isEmpty()) return Optional.empty();

        var size = values.size();
        return Optional.of(values.get(random.nextInt(size)));
    }
}
