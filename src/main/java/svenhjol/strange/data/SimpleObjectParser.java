package svenhjol.strange.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public interface SimpleObjectParser {
    RandomSource random();

    String namespace();

    default String parseString(Object s) {
        return String.valueOf(s);
    }

    default boolean parseBoolean(Object b) {
        if (b instanceof Boolean bn) {
            return bn;
        } else if (b instanceof String str) {
            var sl = str.toLowerCase(Locale.ROOT);
            if (sl.equals("false")) {
                return false;
            } else if (sl.equals("true")) {
                return true;
            }
        } else if (b instanceof Integer in) {
            return in != 0;
        }
        throw new RuntimeException("Could not parse bool");
    }

    default int parseInteger(Object i) {
        if (i instanceof Integer in) {
            return in;
        } else if (i instanceof Double d) {
            return d.intValue();
        } else if (i instanceof Float f) {
            return f.intValue();
        } else if (i instanceof String str) {
            if (str.contains("-")) {
                // Range
                var split = Arrays.stream(str.split("-")).map(Integer::parseInt).toList();
                return random().nextIntBetweenInclusive(split.get(0), split.get(1));
            } else {
                return Integer.parseInt(str);
            }
        }
        throw new RuntimeException("Could not parse int");
    }

    default double parseDouble(Object d) {
        if (d instanceof Double dl) {
            return dl;
        } else if (d instanceof Integer i) {
            return i.doubleValue();
        } else if (d instanceof Float f) {
            return f.doubleValue();
        } else if (d instanceof String str) {
            if (str.endsWith("d")) {
                str = str.substring(0, str.lastIndexOf("d"));
            }
            return Double.parseDouble(str);
        }
        throw new RuntimeException("Could not parse double");
    }

    default ResourceLocation parseResourceLocation(Object r) {
        if (r instanceof ResourceLocation res) {
            return res;
        } else if (r instanceof String str) {
            return new ResourceLocation(str);
        }

        throw new RuntimeException("Could not parse string: " + r);
    }

    @SuppressWarnings("unchecked")
    default List<ResourceLocation> parseResourceLocationList(Object rs) {
        if (rs instanceof List<?> rsl) {
            return (List<ResourceLocation>) rsl;
        } else if (rs instanceof String str) {
            return Arrays.stream(str.split(",")).map(this::parseResourceLocation).toList();
        }

        throw new RuntimeException("Could not parse string: " + rs);
    }

    default Item parseItem(Object i) {
        if (!(i instanceof String str)) {
            throw new RuntimeException("Could not parse string: " + i);
        }

        return BuiltInRegistries.ITEM.get(parseResourceLocation(str));
    }
}
