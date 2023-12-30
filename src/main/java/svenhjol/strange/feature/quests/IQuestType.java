package svenhjol.strange.feature.quests;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import svenhjol.charmony.helper.TagHelper;

import java.util.ArrayList;
import java.util.List;

public interface IQuestType<T> {
    Registry<T> registry();

    ResourceKey<Registry<T>> resourceKey();

    TagKey<T> tag(ResourceLocation id);

    default List<T> tagValues(ResourceLocation id) {
        return TagHelper.getValues(registry(), tag(id));
    }

    default List<ResourceLocation> tagValueIds(ResourceLocation id) {
        return registryValuesToIds(tagValues(id));
    }

    default List<T> idsToRegistryValues(List<ResourceLocation> values) {
        List<T> out = new ArrayList<>();

        for (ResourceLocation value : values) {
            out.add(registry().get(value));
        }

        return out;
    }

    default List<ResourceLocation> registryValuesToIds(List<T> values) {
        List<ResourceLocation> out = new ArrayList<>();

        for (T value : values) {
            out.add(registry().getKey(value));
        }

        return out;
    }
}
