package svenhjol.strange.scrolls.quest;

import com.google.gson.Gson;
import net.minecraft.resources.IResource;
import net.minecraft.world.World;
import svenhjol.strange.scrolls.module.Quests;
import svenhjol.strange.scrolls.quest.action.Gather;
import svenhjol.strange.scrolls.quest.action.Hunt;
import svenhjol.strange.scrolls.quest.iface.IQuest;
import svenhjol.strange.scrolls.quest.limit.Time;
import svenhjol.strange.scrolls.quest.reward.RewardItem;
import svenhjol.strange.scrolls.quest.reward.XP;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Generator
{
    public static Generator INSTANCE = new Generator();

    public IQuest generate(World world, Definition definition)
    {
        return generate(world, definition, null);
    }

    public IQuest generate(World world, Definition definition, @Nullable IQuest quest)
    {
        if (quest == null) quest = new Quest();

        quest.setTier(definition.getTier());
        quest.setTitle(definition.getTitle());
        quest.setDescription(definition.getDescription());

        Criteria criteria = quest.getCriteria();

        (new Gather()).fromDefinition(definition).forEach(criteria::addCondition);
        (new Hunt()).fromDefinition(definition).forEach(criteria::addCondition);
        (new RewardItem(world)).fromDefinition(definition).forEach(criteria::addCondition);
        (new Time()).fromDefinition(definition).forEach(criteria::addCondition);
        (new XP()).fromDefinition(definition).forEach(criteria::addCondition);

        return quest;
    }

    public IQuest generate(World world, IQuest quest)
    {
        List<Definition> definitions = Quests.available.get(quest.getTier());
        Definition definition = definitions.get(world.rand.nextInt(definitions.size()));

        try {
            return generate(world, definition);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Definition deserialize(IResource resource)
    {
        Reader reader = new InputStreamReader(resource.getInputStream());
        return new Gson().fromJson(reader, Definition.class);
    }

    public class Definition
    {
        public int tier;
        public String title;
        public String description;
        public Map<String, String> gather = new HashMap<>();
        public Map<String, String> hunt = new HashMap<>();
        public Map<String, String> rewardItems = new HashMap<>();
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

        public Map<String, String> getGather()
        {
            return gather == null ? new HashMap<>() : gather;
        }

        public Map<String, String> getHunt()
        {
            return hunt == null ? new HashMap<>() : hunt;
        }

        public Map<String, String> getRewardItems()
        {
            return rewardItems == null ? new HashMap<>() : rewardItems;
        }

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
    }
}
