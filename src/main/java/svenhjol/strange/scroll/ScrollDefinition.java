package svenhjol.strange.scroll;

import com.google.gson.Gson;
import net.minecraft.resource.Resource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScrollDefinition {
    private int tier;
    private int timeLimit; // in minutes
    private boolean builtIn;
    private String title;
    private List<String> modules;
    private List<String> locate = new ArrayList<>();
    private Map<String, String> gather = new HashMap<>();
    private Map<String, String> hunt = new HashMap<>();
    private Map<String, Map<String, String>> encounter = new HashMap<>();
    private Map<String, Map<String, String>> reward = new HashMap<>();
    private Map<String, Map<String, String>> lang = new HashMap<>();

    public String getTitle() {
        return title;
    }

    public int getTier() {
        return tier;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public boolean isBuiltIn() {
        return builtIn;
    }

    public List<String> getModules() {
        return modules == null ? new ArrayList<>() : modules;
    }

    public List<String> getLocate() {
        return locate == null ? new ArrayList<>() : locate;
    }

    public Map<String, String> getGather() {
        return gather == null ? new HashMap<>() : gather;
    }

    public Map<String, String> getHunt() {
        return hunt == null ? new HashMap<>() : hunt;
    }

    public Map<String, Map<String, String>> getEncounter() {
        return encounter == null ? new HashMap<>() : encounter;
    }

    public Map<String, Map<String, String>> getReward() {
        return reward == null ? new HashMap<>() : reward;
    }

    public Map<String, Map<String, String>> getLang() {
        return lang == null ? new HashMap<>() : lang;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static ScrollDefinition deserialize(Resource resource) {
        Reader reader = new InputStreamReader(resource.getInputStream());
        return new Gson().fromJson(reader, ScrollDefinition.class);
    }
}
