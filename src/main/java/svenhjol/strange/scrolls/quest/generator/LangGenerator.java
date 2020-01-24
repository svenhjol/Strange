package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.Definition;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LangGenerator extends BaseGenerator
{
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";

    public LangGenerator(World world, BlockPos pos, IQuest quest, Definition definition)
    {
        super(world, pos, quest, definition);
    }

    @Override
    public void generate()
    {
        Map<String, Map<String, String>> def = definition.getLang();
        if (def.isEmpty()) return;

        if (def.containsKey(Quests.language)) {
            Map<String, String> strings = def.get(Quests.language);
            List<String> keys = new ArrayList<>(strings.keySet());
            List<String> descriptions = new ArrayList<>();

            if (keys.contains(TITLE)) {
                quest.setTitle(strings.get(TITLE));
            }
            if (keys.contains(DESCRIPTION)) {
                descriptions.add(strings.get(DESCRIPTION));
            }

            for (int i = 0; i < 4; i++) {
                String key = DESCRIPTION + i;
                if (keys.contains(key))
                    descriptions.add(strings.get(DESCRIPTION));
            }

            if (!descriptions.isEmpty())
                quest.setDescription(descriptions.get(world.rand.nextInt(descriptions.size())));
        }
    }
}
