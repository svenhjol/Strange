package svenhjol.strange.feature.quests.type;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import svenhjol.strange.feature.quests.IQuestType;

import java.util.HashMap;
import java.util.Map;

public class HuntType implements IQuestType<EntityType<?>> {
    static final HuntType INSTANCE = new HuntType();
    static final Map<ResourceLocation, TagKey<EntityType<?>>> TAGS = new HashMap<>();

    public static HuntType instance() {
        return INSTANCE;
    }

    @Override
    public Registry<EntityType<?>> registry() {
        return BuiltInRegistries.ENTITY_TYPE;
    }

    @Override
    public ResourceKey<Registry<EntityType<?>>> resourceKey() {
        return Registries.ENTITY_TYPE;
    }

    @Override
    public TagKey<EntityType<?>> tag(ResourceLocation id) {
        if (!TAGS.containsKey(id)) {
            TAGS.put(id, TagKey.create(resourceKey(), id));
        }
        return TAGS.get(id);
    }
}
