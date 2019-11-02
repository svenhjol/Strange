package svenhjol.strange.scrolls.quest;

import com.google.gson.Gson;
import net.minecraft.resources.IResource;
import net.minecraft.world.World;
import svenhjol.meson.Meson;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.generator.*;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

public class Generator
{
    public static Generator INSTANCE = new Generator();

    public IQuest generate(World world, Definition definition, @Nullable IQuest quest)
    {
        if (quest == null) quest = new Quest();

        quest.setTier(definition.getTier());
        quest.setTitle(definition.getTitle());
        quest.setDescription(definition.getDescription());

        // initialise generators
        List<Class<?>> generators = new ArrayList<>(Arrays.asList(
            EncounterGenerator.class,
            GatherGenerator.class,
            CraftGenerator.class,
            HuntGenerator.class,
            RewardItemGenerator.class,
            TimeGenerator.class,
            XPGenerator.class
        ));

        for (Class<?> clazz : generators) {
            try {
                BaseGenerator gen = (BaseGenerator) clazz.getConstructor(World.class, IQuest.class, Definition.class).newInstance(world, quest, definition);
                gen.generate();
            } catch (Exception e) {
                Meson.warn("Could not initialize generator " + clazz + ", skipping");
            }
        }

        return quest;
    }

    public IQuest generate(World world, IQuest quest)
    {
        List<Definition> definitions = Quests.available.get(quest.getTier());
        Definition definition = definitions.get(world.rand.nextInt(definitions.size()));

        try {
            return generate(world, definition, quest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class Definition
    {
        public int tier;
        public String title;
        public String description;
        public Map<String, Map<String, String>> encounter = new HashMap<>();
        public Map<String, String> gather = new HashMap<>();
        public Map<String, String> craft = new HashMap<>();
        public Map<String, String> hunt = new HashMap<>();
        public Map<String, String> rewardItems = new HashMap<>();
        public Map<String, String> rewardConfig = new HashMap<>();
        public int xp;
        public int timeLimit; // in minutes

        public int getTier()
        {
            return tier;
        }

        public String getTitle()
        {
            return title == null ? "" : title;
        }

        public String getDescription()
        {
            return description == null ? "" : description;
        }

        public Map<String, Map<String, String>> getEncounter()
        {
            return encounter == null ? new HashMap<>() : encounter;
        }

        public Map<String, String> getGather()
        {
            return gather == null ? new HashMap<>() : gather;
        }

        public Map<String, String> getCraft()
        {
            return craft == null ? new HashMap<>() : craft;
        }

        public Map<String, String> getHunt()
        {
            return hunt == null ? new HashMap<>() : hunt;
        }

        public Map<String, String> getRewardItems()
        {
            return rewardItems == null ? new HashMap<>() : rewardItems;
        }

        public Map<String, String> getRewardConfig()
        {
            return rewardConfig == null ? new HashMap<>() : rewardConfig;
        }

        // TODO rework in favor of scaled difficulty
        public int parseCount(String countDef)
        {
            if (countDef.contains("-")) { // it's a range
                String[] parts = countDef.split("-");
                int min = Integer.parseInt(parts[0]);
                int max = Integer.parseInt(parts[1]);
                return (new Random().nextInt(max - min) + min) + 1;
            } else {
                return Integer.parseInt(countDef);
            }
        }

        public static Definition deserialize(IResource resource)
        {
            Reader reader = new InputStreamReader(resource.getInputStream());
            return new Gson().fromJson(reader, Definition.class);
        }
    }
}
