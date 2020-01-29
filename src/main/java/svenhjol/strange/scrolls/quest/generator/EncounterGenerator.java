package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Definition;
import svenhjol.strange.scrolls.quest.condition.Encounter;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EncounterGenerator extends BaseGenerator
{
    public static final String COUNT = "count";
    public static final String HEALTH = "health";
    public static final String EFFECTS = "effects";

    public EncounterGenerator(World world, BlockPos pos, IQuest quest, Definition definition)
    {
        super(world, pos, quest, definition);
    }

    @Override
    public void generate()
    {
        Map<String, Map<String, String>> def = definition.getEncounter();
        if (def.isEmpty()) return;

        Condition<Encounter> condition = Condition.factory(Encounter.class, quest);
        Encounter encounter = condition.getDelegate();

        for (String key : def.keySet()) { // key is the target enemy type
            ResourceLocation target = getEntityResFromKey(key);
            if (target == null) continue;

            final Map<String, String> data = def.get(key);
            final int count = data.containsKey(COUNT) ? getCountFromValue(data.get(COUNT), false) : 0;
            final int health = data.containsKey(HEALTH) ? getCountFromValue(data.get(HEALTH), true) : 0;

            String effectDefs = data.getOrDefault(EFFECTS, "");
            List<String> effects = new ArrayList<>();
            if (!effectDefs.isEmpty()) {
                if (effectDefs.contains(",")) {
                    effects = new ArrayList<>(Arrays.asList(effectDefs.split(",")));
                } else {
                    effects.add(effectDefs);
                }
            }

            encounter.addTarget(target, count, health, effects);
        }

        encounter.bossInfo.setDarkenSky(true);
        encounter.bossInfo.setCreateFog(true);
        encounter.bossInfo.setColor(BossInfo.Color.BLUE);
        quest.getCriteria().addCondition(condition);
    }
}
