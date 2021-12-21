package svenhjol.strange.module.quests.definition;

import com.google.gson.Gson;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.packs.resources.Resource;
import svenhjol.strange.helper.NbtHelper;
import svenhjol.strange.module.runes.Tier;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestDefinition {
    public static final String TIER_TAG = "Tier";
    public static final String ID_TAG = "Id";
    public static final String TEST_TAG = "Test";
    public static final String MODULES_TAG = "Modules";
    public static final String DIMENSIONS_TAG = "Dimension";
    public static final String LANG_TAG = "Lang";
    public static final String HUNT_TAG = "Hunt";
    public static final String GATHER_TAG = "Gather";
    public static final String EXPLORE_TAG = "Explore";
    public static final String BOSS_TAG = "Explore";
    public static final String REWARD_TAG = "Reward";

    private boolean test;
    private int tier;
    private String id;
    private List<String> modules = new ArrayList<>();
    private List<String> dimensions = new ArrayList<>();
    private Map<String, Map<String, String>> lang = new HashMap<>();
    private Map<String, String> hunt = new HashMap<>(); // TODO: why are you this
    private Map<String, Map<String, Map<String, String>>> gather = new HashMap<>();
    private Map<String, Map<String, Map<String, String>>> explore = new HashMap<>();
    private Map<String, Map<String, Map<String, String>>> boss = new HashMap<>();
    private Map<String, Map<String, Map<String, String>>> reward = new HashMap<>();

    public static QuestDefinition deserialize(Resource resource) {
        Reader reader = new InputStreamReader(resource.getInputStream());
        return new Gson().fromJson(reader, QuestDefinition.class);
    }

    public CompoundTag save() {
        var tag = new CompoundTag();

        tag.putInt(TIER_TAG, tier);
        tag.putString(ID_TAG, id);
        tag.putBoolean(TEST_TAG, test);
        tag.put(MODULES_TAG, NbtHelper.packStrings(modules));
        tag.put(DIMENSIONS_TAG, NbtHelper.packStrings(dimensions));
        tag.put(HUNT_TAG, NbtHelper.packMap(hunt));
        tag.put(LANG_TAG, NbtHelper.packDoubleNested(lang));
        tag.put(GATHER_TAG, NbtHelper.packTripleNested(gather));
        tag.put(EXPLORE_TAG, NbtHelper.packTripleNested(explore));
        tag.put(BOSS_TAG, NbtHelper.packTripleNested(boss));
        tag.put(REWARD_TAG, NbtHelper.packTripleNested(reward));

        return tag;
    }

    public static QuestDefinition load(CompoundTag tag) {
        var definition = new QuestDefinition();

        definition.tier = tag.getInt(TIER_TAG);
        definition.id = tag.getString(ID_TAG);
        definition.test = tag.getBoolean(TEST_TAG);
        definition.modules = NbtHelper.unpackStrings(tag.getCompound(MODULES_TAG));
        definition.dimensions = NbtHelper.unpackStrings(tag.getCompound(DIMENSIONS_TAG));
        definition.hunt = NbtHelper.unpackMap(tag.getCompound(HUNT_TAG));
        definition.lang = NbtHelper.unpackDoubleNested(tag.getCompound(LANG_TAG));
        definition.gather = NbtHelper.unpackTripleNested(tag.getCompound(GATHER_TAG));
        definition.explore = NbtHelper.unpackTripleNested(tag.getCompound(EXPLORE_TAG));
        definition.boss = NbtHelper.unpackTripleNested(tag.getCompound(BOSS_TAG));
        definition.reward = NbtHelper.unpackTripleNested(tag.getCompound(REWARD_TAG));

        return definition;
    }

    public String getId() {
        return id;
    }

    public Tier getTier() {
        return Tier.byLevel(tier);
    }

    public boolean isTest() {
        return test;
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

    public void setTier(Tier tier) {
        this.tier = tier.getLevel();
    }

    private CompoundTag packList(List<String> list) {
        return NbtHelper.packStrings(list);
    }

    private List<String> unpackList(CompoundTag tag) {
        return NbtHelper.unpackStrings(tag);
    }
}
