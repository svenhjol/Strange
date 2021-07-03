package svenhjol.strange.module.scrolls.populator;

import net.minecraft.server.level.ServerPlayer;
import svenhjol.strange.module.scrolls.Scrolls;
import svenhjol.strange.module.scrolls.nbt.Quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LangPopulator extends BasePopulator {
    public static final String TITLE_NBT = "title";
    public static final String DESCRIPTION_NBT = "description";
    public static final String HINT_NBT = "hint";

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

            if (keys.contains(TITLE_NBT))
                quest.setTitle(strings.get(TITLE_NBT));

            if (keys.contains(DESCRIPTION_NBT))
                quest.setDescription(strings.get(DESCRIPTION_NBT));

            if (keys.contains(HINT_NBT))
                quest.setHint(strings.get(HINT_NBT));
        }
    }
}
