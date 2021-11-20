package svenhjol.strange.module.knowledge_stones;

import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.knowledge.KnowledgeHelper.LearnableKnowledgeType;

import java.util.HashMap;
import java.util.Map;

@CommonModule(mod = Strange.MOD_ID)
public class KnowledgeStones extends CharmModule {
    public static Map<LearnableKnowledgeType, KnowledgeStoneItem> KNOWLEDGE_STONES = new HashMap<>();

    @Override
    public void register() {
        for (LearnableKnowledgeType type : LearnableKnowledgeType.values()) {
            KNOWLEDGE_STONES.put(type, new KnowledgeStoneItem(this, type));
        }
    }
}
