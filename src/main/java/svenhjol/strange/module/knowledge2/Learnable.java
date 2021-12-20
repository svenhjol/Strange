package svenhjol.strange.module.knowledge2;

import svenhjol.charm.enums.ICharmEnum;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Learnable implements ICharmEnum {
    RUNE,
    BIOME,
    STRUCTURE,
    DIMENSION;

    public static List<String> getNames() {
        return Arrays.stream(Learnable.values()).map(ICharmEnum::getNameAsString).collect(Collectors.toList());
    }
}
