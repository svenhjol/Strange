package svenhjol.strange.module.knowledge_stones;

import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

@CommonModule(mod = Strange.MOD_ID)
public class KnowledgeStones extends CharmModule {
    public static Map<KnowledgeStoneItem.Type, KnowledgeStoneItem> KNOWLEDGE_STONES = new HashMap<>();

    @Override
    public void register() {
        for (KnowledgeStoneItem.Type type : KnowledgeStoneItem.Type.values()) {
            KNOWLEDGE_STONES.put(type, new KnowledgeStoneItem(this, type));
        }
    }
}
