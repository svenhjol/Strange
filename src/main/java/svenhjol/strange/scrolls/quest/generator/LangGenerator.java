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
    public static final String HINT = "hint";

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

            if (keys.contains(TITLE))
                quest.setTitle(strings.get(TITLE));

            if (keys.contains(DESCRIPTION)) {
                String description = strings.get(DESCRIPTION);
                quest.setDescription(splitOptionalRandomly(description));
            }

            if (keys.contains(HINT)) {
                String hint = quest.getDescription() + "%%" + strings.get(HINT);
                quest.setDescription(hint);
            }
        }
    }
}
