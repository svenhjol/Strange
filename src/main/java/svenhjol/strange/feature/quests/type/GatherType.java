package svenhjol.strange.feature.quests.type;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import svenhjol.strange.feature.quests.IQuestType;

import java.util.HashMap;
import java.util.Map;

public class GatherType implements IQuestType<Item> {
    static final GatherType INSTANCE = new GatherType();
    static final Map<ResourceLocation, TagKey<Item>> TAGS = new HashMap<>();

    public static GatherType instance() {
        return INSTANCE;
    }

    @Override
    public Registry<Item> registry() {
        return BuiltInRegistries.ITEM;
    }

    @Override
    public ResourceKey<Registry<Item>> resourceKey() {
        return Registries.ITEM;
    }

    @Override
    public TagKey<Item> tag(ResourceLocation id) {
        if (!TAGS.containsKey(id)) {
            TAGS.put(id, TagKey.create(resourceKey(), id));
        }
        return TAGS.get(id);
    }


}
