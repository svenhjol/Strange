package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Generator;
import svenhjol.strange.scrolls.quest.condition.Encounter;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EncounterGenerator extends BaseGenerator
{
    public EncounterGenerator(World world, BlockPos pos, IQuest quest, Generator.Definition definition)
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

        for (String targetStr : def.keySet()) {
            ResourceLocation target = new ResourceLocation(targetStr);
            Map<String, String> data = def.get(targetStr);
            int count = data.containsKey("count") ? Integer.parseInt(data.get("count")) : 0;
            int health = data.containsKey("health") ? Integer.parseInt(data.get("health")) : 0;
            String effectDefs = data.getOrDefault("effects", "");
            List<String> effects = new ArrayList<>();
            if (!effectDefs.isEmpty()) {
                if (effectDefs.contains(",")) {
                    effects = new ArrayList<>(Arrays.asList(effectDefs.split(",")));
                } else {
                    effects.add(effectDefs);
                }
            }

            // health scales on distance
            health = multiplyDistance(health);

            encounter.addTarget(target, count, health, effects);
        }

        encounter.bossInfo.setDarkenSky(true);
        encounter.bossInfo.setColor(BossInfo.Color.BLUE);
        quest.getCriteria().addCondition(condition);
    }
}
