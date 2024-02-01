package svenhjol.strange.feature.quests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import svenhjol.strange.data.ResourceListManager;
import svenhjol.strange.data.SimpleObjectParser;
import svenhjol.strange.feature.quests.QuestDefinition;

import java.util.LinkedList;
import java.util.Map;

public abstract class BaseDefinition<T> implements SimpleObjectParser {
    protected QuestDefinition definition;

    public BaseDefinition(QuestDefinition definition) {
        this.definition = definition;
    }

    public abstract T fromMap(Map<String, Object> map);

    protected abstract String dataDir();

    protected Map<ResourceLocation, LinkedList<ResourceLocation>> entries() {
        return ResourceListManager.entries(definition.manager(), dataDir());
    }

    public QuestDefinition questDefinition() {
        return definition;
    }

    @Override
    public RandomSource random() {
        return questDefinition().random();
    }

    @Override
    public String namespace() {
        return questDefinition().namespace();
    }
}
