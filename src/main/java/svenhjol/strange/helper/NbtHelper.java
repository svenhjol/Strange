package svenhjol.strange.helper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NbtHelper {
    public static final String LIST = "List";

    public static CompoundTag packStrings(List<String> input) {
        var tag = new CompoundTag();
        var list = new ListTag();

        list.addAll(input.stream()
            .map(StringTag::valueOf)
            .collect(Collectors.toList()));

        tag.put(LIST, list);
        return tag;
    }

    public static List<String> unpackStrings(CompoundTag tag) {
        if (!tag.contains(LIST)) return List.of();

        List<String> out = new ArrayList<>();
        var list = (ListTag)tag.get(LIST);
        if (list == null) return List.of();

        for (int i = 0; i < list.size(); i++) {
            out.add(list.getString(i));
        }

        return out;
    }

    public static CompoundTag packMap(Map<String, String> map) {
        var tag = new CompoundTag();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            var key = entry.getKey();
            var val = entry.getValue();
            tag.putString(key, val);
        }

        return tag;
    }

    public static Map<String, String> unpackMap(CompoundTag tag) {
        Map<String, String> out = new HashMap<>();

        for (String key : tag.getAllKeys()) {
            var val = tag.getString(key);
            out.put(key, val);
        }

        return out;
    }

    public static CompoundTag packDoubleNested(Map<String, Map<String, String>> map) {
        var tag = new CompoundTag();

        for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
            var key1 = entry.getKey();
            var tag1 = new CompoundTag();

            for (Map.Entry<String, String> val1 : entry.getValue().entrySet()) {
                var key2 = val1.getKey();
                var val2 = val1.getValue();

                tag1.putString(key2, val2);
            }
            tag.put(key1, tag1);
        }

        return tag;
    }

    public static Map<String, Map<String, String>> unpackDoubleNested(CompoundTag tag) {
        Map<String, Map<String, String>> out = new HashMap<>();

        for (String key1 : tag.getAllKeys()) {
            var tag1 = tag.getCompound(key1);

            for (String key2 : tag1.getAllKeys()) {
                var val2 = tag1.getString(key2);

                out.computeIfAbsent(key1, m -> new HashMap<>()).put(key2, val2);
            }
        }

        return out;
    }

    public static CompoundTag packTripleNested(Map<String, Map<String, Map<String, String>>> map) {
        var tag = new CompoundTag();

        for (Map.Entry<String, Map<String, Map<String, String>>> entry : map.entrySet()) {
            var key1 = entry.getKey();
            var tag1 = new CompoundTag();

            for (Map.Entry<String, Map<String, String>> val1 : entry.getValue().entrySet()) {
                var key2 = val1.getKey();
                var tag2 = new CompoundTag();

                for (Map.Entry<String, String> val2 : val1.getValue().entrySet()) {
                    var key3 = val2.getKey();
                    var val3 = val2.getValue();

                    tag2.putString(key3, val3);
                }
                tag1.put(key2, tag2);
            }
            tag.put(key1, tag1);
        }

        return tag;
    }

    public static Map<String, Map<String, Map<String, String>>> unpackTripleNested(CompoundTag tag) {
        Map<String, Map<String, Map<String, String>>> out = new HashMap<>();

        for (String key1 : tag.getAllKeys()) {
            var tag1 = tag.getCompound(key1);

            for (String key2 : tag1.getAllKeys()) {
                var tag2 = tag1.getCompound(key2);

                for (String key3 : tag2.getAllKeys()) {
                    var val3 = tag2.getString(key3);

                    out.computeIfAbsent(key1, m -> new HashMap<>()).computeIfAbsent(key2, m -> new HashMap<>()).put(key3, val3);
                }
            }
        }

        return out;
    }
}
