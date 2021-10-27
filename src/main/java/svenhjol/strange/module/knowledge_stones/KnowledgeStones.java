package svenhjol.strange.module.knowledge_stones;

import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;

@CommonModule(mod = Strange.MOD_ID)
public class KnowledgeStones extends CharmModule {
    public static KnowledgeStoneItem KNOWLEDGE_STONE;

    @Override
    public void register() {
        KNOWLEDGE_STONE = new KnowledgeStoneItem(this);
    }
}
