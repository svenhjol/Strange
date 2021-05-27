package svenhjol.strange.module.scrolls;

import com.google.gson.Gson;
import net.minecraft.resource.Resource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScrollDefinition {
    private String id;
    private int tier;
    private int time_limit; // in minutes
    private boolean default_pack;
    private String title = "";
    private String description = "";
    private String hint = "";
    private List<String> valid_dimensions = new ArrayList<>();
    private List<String> modules = new ArrayList<>();
    private Map<String, String> hunt = new HashMap<>();
    private Map<String, Map<String, Map<String, String>>> gather = new HashMap<>();
    private Map<String, Map<String, Map<String, String>>> explore = new HashMap<>();
    private Map<String, Map<String, Map<String, String>>> boss = new HashMap<>();
    private Map<String, Map<String, Map<String, String>>> reward = new HashMap<>();
    private Map<String, Map<String, String>> lang = new HashMap<>();

    public String getId() {
        return id;
    }

    public int getTier() {
        return tier;
    }

    public int getTimeLimit() {
        if (time_limit == 0)
            time_limit = QuestManager.DEFAULT_EXPIRY;

        return time_limit * 60 * 20; // in ticks
    }

    public boolean isDefaultPack() {
        return default_pack;
    }

    public List<String> getValidDimensions() {
        return valid_dimensions;
    }

    public List<String> getModules() {
        return modules == null ? new ArrayList<>() : modules;
    }

    public Map<String, String> getHunt() {
        return hunt == null ? new HashMap<>() : hunt;
    }

    public Map<String, Map<String, Map<String, String>>> getGather() {
        return gather == null ? new HashMap<>() : gather;
    }

    public Map<String, Map<String, Map<String, String>>> getExplore() {
        return explore == null ? new HashMap<>() : explore;
    }

    public Map<String, Map<String, Map<String, String>>> getBoss() {
        return boss == null ? new HashMap<>() : boss;
    }

    public Map<String, Map<String, Map<String, String>>> getReward() {
        return reward == null ? new HashMap<>() : reward;
    }

    public Map<String, Map<String, String>> getLang() {
        return lang == null ? new HashMap<>() : lang;
    }

    public void setId(String id) {
        this.id = id;
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
