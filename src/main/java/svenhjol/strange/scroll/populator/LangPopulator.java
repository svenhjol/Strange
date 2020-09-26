package svenhjol.strange.scroll.populator;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.strange.module.Scrolls;
import svenhjol.strange.scroll.BasePopulator;
import svenhjol.strange.scroll.JsonDefinition;
import svenhjol.strange.scroll.tag.QuestTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LangPopulator extends BasePopulator {
    public static final String TITLE = "title";

    public LangPopulator(World world, BlockPos pos, QuestTag quest, JsonDefinition definition) {
        super(world, pos, quest, definition);
    }

    @Override
    public void populate() {
        Map<String, Map<String, String>> def = definition.getLang();
        if (def.isEmpty())
            return;

        if (def.containsKey(Scrolls.language)) {
            Map<String, String> strings = def.get(Scrolls.language);
            List<String> keys = new ArrayList<>(strings.keySet());

            if (keys.contains(TITLE))
                quest.setTitle(strings.get(TITLE));
        }
    }
}
