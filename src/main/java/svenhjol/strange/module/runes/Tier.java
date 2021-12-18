package svenhjol.strange.module.runes;

import svenhjol.charm.enums.ICharmEnum;

import javax.annotation.Nullable;
import java.util.List;

public enum Tier implements ICharmEnum {
    TEST(List.of()),
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

    @Nullable
    public static Tier getByOrdinal(int o) {
        for (Tier value : values()) {
            if (value.ordinal() == o) {
                return value;
            }
        }
        return null;
    }
}
