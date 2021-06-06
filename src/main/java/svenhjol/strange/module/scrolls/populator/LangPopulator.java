package svenhjol.strange.module.scrolls.populator;

import svenhjol.strange.module.scrolls.Scrolls;
import svenhjol.strange.module.scrolls.tag.Quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;

public class LangPopulator extends BasePopulator {
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String HINT = "hint";

    public LangPopulator(ServerPlayer player, Quest quest) {
        super(player, quest);
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

            if (keys.contains(DESCRIPTION))
                quest.setDescription(strings.get(DESCRIPTION));

            if (keys.contains(HINT))
                quest.setHint(strings.get(HINT));
        }
    }
}
