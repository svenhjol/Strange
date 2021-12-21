package svenhjol.strange.helper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.ArrayList;
import java.util.List;
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

        for (int i = 0; i < list.size(); i++) {
            out.add(list.getString(i));
        }

        return out;
    }
}
