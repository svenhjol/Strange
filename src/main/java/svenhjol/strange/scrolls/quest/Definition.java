package svenhjol.strange.scrolls.quest;

import com.google.gson.Gson;
import net.minecraft.resources.IResource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * When creating a new condition delegate, add the delegate definition here
 * so that you can describe the condition in the JSON quest file.
 */
public class Definition
{
    public int tier;
    public String title;
    public String description;
    public int timeLimit; // in minutes
    public List<String> locate = new ArrayList<>();
    public Map<String, String> craft = new HashMap<>();
    public Map<String, String> gather = new HashMap<>();
    public Map<String, String> hunt = new HashMap<>();
    public Map<String, String> mine = new HashMap<>();
    public Map<String, Map<String, String>> encounter = new HashMap<>();
    public Map<String, Map<String, String>> rewards = new HashMap<>();

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

    public List<String> getLocate()
    {
        return locate == null ? new ArrayList<>() : locate;
    }

    public Map<String, String> getCraft()
    {
        return craft == null ? new HashMap<>() : craft;
    }

    public Map<String, String> getGather()
    {
        return gather == null ? new HashMap<>() : gather;
    }

    public Map<String, String> getHunt()
    {
        return hunt == null ? new HashMap<>() : hunt;
    }

    public Map<String, String> getMine()
    {
        return mine == null ? new HashMap<>() : mine;
    }

    public Map<String, Map<String, String>> getEncounter()
    {
        return encounter == null ? new HashMap<>() : encounter;
    }

    public Map<String, Map<String, String>> getRewards()
    {
        return rewards == null ? new HashMap<>() : rewards;
    }

    public static Definition deserialize(IResource resource)
    {
        Reader reader = new InputStreamReader(resource.getInputStream());
        return new Gson().fromJson(reader, Definition.class);
    }
}
