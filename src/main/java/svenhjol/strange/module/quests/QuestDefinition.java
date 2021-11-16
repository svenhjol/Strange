package svenhjol.strange.module.quests;

import com.google.gson.Gson;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestDefinition {
    private int tier;
    private String id;
    private String pack;
    private String title;
    private String description;
    private String hint;
    private List<String> modules = new ArrayList<>();
    private List<String> dimensions = new ArrayList<>();
    private Map<String, Map<String, String>> lang = new HashMap<>();

    // TODO: why are you this
    private Map<String, String> hunt = new HashMap<>();
    private Map<String, Map<String, Map<String, String>>> gather = new HashMap<>();
    private Map<String, Map<String, Map<String, String>>> explore = new HashMap<>();
    private Map<String, Map<String, Map<String, String>>> boss = new HashMap<>();
    private Map<String, Map<String, Map<String, String>>> reward = new HashMap<>();

    public static QuestDefinition deserialize(Resource resource) {
        Reader reader = new InputStreamReader(resource.getInputStream());
        return new Gson().fromJson(reader, QuestDefinition.class);
    }

    public String getId() {
        return id;
    }

    public int getTier() {
        return tier;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getHint() {
        return hint;
    }

    public String getPack() {
        return pack;
    }

    public List<String> getModules() {
        return modules;
    }

    public List<String> getDimensions() {
        return dimensions;
    }

    public Map<String, Map<String, String>> getLang() {
        return lang;
    }

    public Map<String, String> getHunt() {
        return hunt;
    }

    public Map<String, Map<String, Map<String, String>>> getGather() {
        return gather;
    }

    public Map<String, Map<String, Map<String, String>>> getExplore() {
        return explore;
    }

    public Map<String, Map<String, Map<String, String>>> getBoss() {
        return boss;
    }

    public Map<String, Map<String, Map<String, String>>> getReward() {
        return reward;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }
}
