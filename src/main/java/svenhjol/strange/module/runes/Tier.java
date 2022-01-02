package svenhjol.strange.module.runes;

import svenhjol.charm.enums.ICharmEnum;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public enum Tier implements ICharmEnum {
    NOVICE(List.of('a', 'b', 'c', 'd', 'e', 'f')),
    APPRENTICE(List.of('g', 'h', 'i', 'j', 'k', 'l')),
    JOURNEYMAN(List.of('m', 'n', 'o', 'p', 'q', 'r')),
    EXPERT(List.of('s', 't', 'u', 'v')),
    MASTER(List.of('w', 'x', 'y', 'z'));

    private final List<Character> chars;

    Tier(List<Character> chars) {
        this.chars = chars;
    }

    public List<Character> getChars() {
        return chars;
    }

    public int getLevel() {
        return ordinal() + 1;
    }

    public static int size() {
        return Tier.values().length;
    }

    public static Tier getHighestTier() {
        var tiers = Arrays.asList(values());
        return tiers.get(tiers.size() - 1);
    }

    @Nullable
    public static Tier byName(String name) {
        var first = Arrays.stream(values()).filter(t -> t.getSerializedName().equals(name)).findFirst();
        return first.orElse(null);
    }

    /**
     * Use this method to get the mapping of num -> tier where NOVICE starts at 1.
     */
    @Nullable
    public static Tier byLevel(int l) {
        var first = Arrays.stream(values()).filter(t -> l == t.ordinal() + 1).findFirst();
        return first.orElse(null);
    }

    /**
     * Use this method to get the index of the tier where NOVICE starts at 0.
     */
    @Nullable
    public static Tier byOrdinal(int o) {
        var first = Arrays.stream(values()).filter(t -> t.ordinal() == o).findFirst();
        return first.orElse(null);
    }
}
