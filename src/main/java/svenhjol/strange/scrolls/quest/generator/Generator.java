package svenhjol.strange.scrolls.quest.generator;

import net.minecraft.world.World;
import svenhjol.strange.scrolls.quest.IGenerator;
import svenhjol.strange.scrolls.quest.IQuest;
import svenhjol.strange.scrolls.quest.Quest;
import svenhjol.strange.scrolls.quest.action.Action.Type;

import java.util.*;

public class Generator
{
    private static final String PREFIX = "svenhjol.strange.scrolls.quest.generator.";

    public static IQuest generate(World world, int tier)
    {
        IQuest quest = new Quest();
        quest.setTier(tier);
        quest.setTitle("Level " + tier + " Quest");
        quest.setDescription("Level " + tier + " is super exciting");

        // TODO don't hardcode, select based on tier
        List<Type> types = new ArrayList<>(Arrays.asList(Type.Gather, Type.Hunt));

        // create generators for each type
        Map<Type, IGenerator> generators = new HashMap<>();

        for (Type type : types) {
            if (generators.containsKey(type)) continue;
            generators.put(type, factory(type));
        }

        generators.forEach((type, gen) -> gen.generate(world, quest, tier));

        return quest;
    }

    public static IGenerator factory(Type actionType)
    {
        try {
            String actionName = actionType.getCapitalizedName();
            String className = PREFIX + actionName + "Generator";
            Class clazz = Class.forName(className);

            // noinspection unchecked
            IGenerator generator = (IGenerator)clazz.getConstructor().newInstance();
            return generator;

        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate generator", e);
        }
    }
}
