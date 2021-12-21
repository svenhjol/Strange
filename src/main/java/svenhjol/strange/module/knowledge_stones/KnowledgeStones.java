package svenhjol.strange.module.knowledge_stones;

import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.knowledge.Learnable;

import java.util.HashMap;
import java.util.Map;

@CommonModule(mod = Strange.MOD_ID)
public class KnowledgeStones extends CharmModule {
    public static Map<Learnable, KnowledgeStoneItem> KNOWLEDGE_STONES = new HashMap<>();

    @Override
    public void register() {
        for (Learnable type : Learnable.values()) {
            KNOWLEDGE_STONES.put(type, new KnowledgeStoneItem(this, type));
        }
    }
}
