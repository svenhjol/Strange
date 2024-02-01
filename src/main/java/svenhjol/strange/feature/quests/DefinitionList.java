package svenhjol.strange.feature.quests;

import net.minecraft.Util;
import net.minecraft.util.RandomSource;

import java.util.LinkedList;
import java.util.List;

public class DefinitionList<T extends BaseDefinition<T>> extends LinkedList<T> {
    public List<T> take(int val, RandomSource random) {
        if (isEmpty()) {
            throw new RuntimeException("Can't take from empty list");
        }

        List<T> copy = new LinkedList<>(this);
        Util.shuffle(copy, random);

        var amount = Math.min(val, copy.size());
        return copy.subList(0, amount);
    }

    public T take(RandomSource random) {
        return take(1, random).get(0);
    }
}
