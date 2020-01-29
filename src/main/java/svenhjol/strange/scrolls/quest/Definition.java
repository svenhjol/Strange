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
    private int tier;
    private int timeLimit; // in minutes
    private String title;
    private List<String> modules;
    private List<String> locate = new ArrayList<>();
    private Map<String, String> provide = new HashMap<>();
    private Map<String, String> fetch = new HashMap<>();
    private Map<String, String> craft = new HashMap<>();
    private Map<String, String> gather = new HashMap<>();
    private Map<String, String> hunt = new HashMap<>();
    private Map<String, String> mine = new HashMap<>();
    private Map<String, Map<String, String>> encounter = new HashMap<>();
    private Map<String, Map<String, String>> rewards = new HashMap<>();
    private Map<String, Map<String, String>> lang = new HashMap<>();

    public String getTitle()
    {
        return title;
    }

    public int getTimeLimit()
    {
        return timeLimit;
    }

    public List<String> getModules()
    {
        return modules == null ? new ArrayList<>() : modules;
    }

    public List<String> getLocate()
    {
        return locate == null ? new ArrayList<>() : locate;
    }

    public Map<String, String> getProvide()
    {
        return provide == null ? new HashMap<>() : provide;
    }

    public Map<String, String> getFetch()
    {
        return fetch == null ? new HashMap<>() : fetch;
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

    public Map<String, Map<String, String>> getLang() { return lang == null ? new HashMap<>() : lang; }

    public void setTier(int tier)
    {
        this.tier = tier;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public static Definition deserialize(IResource resource)
    {
        Reader reader = new InputStreamReader(resource.getInputStream());
        return new Gson().fromJson(reader, Definition.class);
    }
}
