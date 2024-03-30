package svenhjol.strange.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;

import java.util.*;

public interface SimpleObjectParser {
    RandomSource random();

    default Optional<String> parseString(Object s) {
        return Optional.of(String.valueOf(s));
    }

    default Optional<Boolean> parseBoolean(Object b) {
        if (b instanceof Boolean bn) {
            return Optional.of(bn);
        } else if (b instanceof String str) {
            var sl = str.toLowerCase(Locale.ROOT);
            if (sl.equals("false")) {
                return Optional.of(false);
            } else if (sl.equals("true")) {
                return Optional.of(true);
            }
        } else if (b instanceof Integer in) {
            return Optional.of(in != 0);
        }
        return Optional.empty();
    }

    default Optional<Integer> parseInteger(Object i) {
        if (i instanceof Integer in) {
            return Optional.of(in);
        } else if (i instanceof Double d) {
            return Optional.of(d.intValue());
        } else if (i instanceof Float f) {
            return Optional.of(f.intValue());
        } else if (i instanceof String str) {
            int out;
            if (str.contains("-")) {
                // Range
                var split = Arrays.stream(str.split("-")).map(Integer::parseInt).toList();
                var min = split.get(0);
                var max = split.get(1);

                if (min == 0 || max == 0) {
                    throw new RuntimeException("Range min or max cannot be zero");
                }

                if (max < min) {
                    var newMax = min;
                    min = max;
                    max = newMax;
                }

                out = random().nextIntBetweenInclusive(min, max);
            } else {
                out = Integer.parseInt(str);
            }
            return Optional.of(out);
        }
        return Optional.empty();
    }

    default Optional<Double> parseDouble(Object d) {
        if (d instanceof Double dl) {
            return Optional.of(dl);
        } else if (d instanceof Integer i) {
            return Optional.of(i.doubleValue());
        } else if (d instanceof Float f) {
            return Optional.of(f.doubleValue());
        } else if (d instanceof String str) {
            if (str.endsWith("d")) {
                str = str.substring(0, str.lastIndexOf("d"));
            }
            return Optional.of(Double.parseDouble(str));
        }
        return Optional.empty();
    }

    default Optional<ResourceLocation> parseResourceLocation(Object r) {
        if (r instanceof ResourceLocation res) {
            return Optional.of(res);
        } else if (r instanceof String str) {
            return Optional.of(new ResourceLocation(str));
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    default Optional<List<ResourceLocation>> parseResourceLocationList(Object rs) {
        if (rs instanceof List<?> rsl) {
            return Optional.of((List<ResourceLocation>) rsl);
        } else if (rs instanceof String str) {
            var list = Arrays.stream(str.split(",")).toList();
            List<ResourceLocation> out = new ArrayList<>();
            for (var res : list) {
                var result = parseResourceLocation(res);
                if (result.isEmpty()) {
                    return Optional.empty();
                }
                out.add(result.get());
            }
            return Optional.of(out);
        }
        return Optional.empty();
    }

    default Optional<Item> parseItem(Object o) {
        var result = parseResourceLocation(o);
        var item = result.map(BuiltInRegistries.ITEM::get);
        if (item.stream().allMatch(i -> i.equals(Items.AIR))) {
            throw new RuntimeException("Invalid item: " + o);
        }
        return item;
    }

    default Optional<MobEffect> parseMobEffect(Object o) {
        var result = parseResourceLocation(o);
        return result.map(BuiltInRegistries.MOB_EFFECT::get);
    }

    default Optional<EntityType<?>> parseEntity(Object o) {
        var result = parseResourceLocation(o);
        return result.map(BuiltInRegistries.ENTITY_TYPE::get);
    }

    default Optional<Potion> parsePotion(Object o) {
        var result = parseResourceLocation(o);
        return result.map(BuiltInRegistries.POTION::get);
    }
}
