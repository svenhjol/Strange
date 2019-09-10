package svenhjol.strange.scrolls.quest;

import net.minecraft.world.World;
import svenhjol.strange.scrolls.quest.action.Gather;
import svenhjol.strange.scrolls.quest.iface.IGenerator;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.*;

public class Generator
{
    private static final String PREFIX = "svenhjol.strange.scrolls.quest.generator.";

    public static IQuest generate(World world, int tier)
    {
//        try {
//            final IResource resource = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(Strange.MOD_ID, "quests/tier1/quest1.json"));
//            Reader reader = new InputStreamReader(resource.getInputStream());
//            final Object json = new Gson().fromJson(reader, Object.class);
//
//
//        } catch (Exception e) {
//            Meson.log("Could not load quest", e);
//        }

        IQuest quest = new Quest();
        quest.setTier(tier);
        quest.setTitle("Level " + tier + " Quest");
        quest.setDescription("Level " + tier + " is super exciting");

        // TODO don't hardcode, select based on tier
        List<String> types = new ArrayList<>(Arrays.asList(Gather.ID));

        // create generators for each type
        Map<String, IGenerator> generators = new HashMap<>();

        for (String type : types) {
            if (generators.containsKey(type)) continue;
            generators.put(type, factory(type));
        }

        generators.forEach((type, gen) -> gen.generate(world, quest, tier));

        return quest;
    }

    public static IGenerator factory(String type)
    {
        try {
            String className = PREFIX + type + "Generator";
            Class clazz = Class.forName(className);

            // noinspection unchecked
            IGenerator generator = (IGenerator)clazz.getConstructor().newInstance();
            return generator;

        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate generator", e);
        }
    }
}
