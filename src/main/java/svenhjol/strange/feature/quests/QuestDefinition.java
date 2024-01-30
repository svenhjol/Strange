package svenhjol.strange.feature.quests;

import com.google.gson.Gson;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection"})
public class QuestDefinition {
    private String namespace = "minecraft";
    private boolean epic = false;
    private int level = 1;
    private int loyalty = 0;
    private String profession;
    private String type;
    private List<String> required_features = new ArrayList<>();
    private List<String> artifact_items = new ArrayList<>();
    private Map<String, Integer> artifact_loot_tables = new HashMap<>();
    private Map<String, Integer> battle_effects = new HashMap<>();
    private Map<String, Integer> battle_mobs = new HashMap<>();
    private Map<String, Integer> gather_items = new HashMap<>();
    private Map<String, Integer> hunt_mobs = new HashMap<>();
    private Map<String, Integer> rewards = new HashMap<>();
    private List<String> reward_functions = new ArrayList<>();
    private int reward_experience = 1;

    public static QuestDefinition deserialize(String namespace, Resource resource) {
        BufferedReader reader;

        try {
            reader = resource.openAsReader();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var def = new Gson().fromJson(reader, QuestDefinition.class);
        def.namespace = namespace;
        return def;
    }

    public Pair<ResourceLocation, Integer> pair(List<Pair<ResourceLocation, Integer>> list, RandomSource random) {
        return list.get(random.nextInt(list.size()));
    }

    public List<ResourceLocation> requiredFeatures() {
        return required_features.stream().map(this::tryParse).toList();
    }

    public List<ResourceLocation> artifactItems() {
        return artifact_items.stream().map(this::tryParse).toList();
    }

    public List<Pair<ResourceLocation, Integer>> artifactLootTables() {
        return convertMapsToPairs(artifact_loot_tables);
    }

    public List<Pair<ResourceLocation, Integer>> battleEffects() {
        return convertMapsToPairs(battle_effects);
    }

    public List<Pair<ResourceLocation, Integer>> battleMobs() {
        return convertMapsToPairs(battle_mobs);
    }

    public List<Pair<ResourceLocation, Integer>> gatherItems() {
        return convertMapsToPairs(gather_items);
    }

    public List<Pair<ResourceLocation, Integer>> huntMobs() {
        return convertMapsToPairs(hunt_mobs);
    }

    public List<Pair<ResourceLocation, Integer>> rewards() {
        return convertMapsToPairs(rewards);
    }

    public List<ResourceLocation> rewardFunctions() {
        return reward_functions.stream().map(this::tryParse).toList();
    }

    public int rewardExperience() {
        return reward_experience;
    }

    public boolean epic() {
        return epic;
    }

    public int level() {
        return level;
    }

    public int loyalty() {
        return loyalty;
    }

    public VillagerProfession profession() {
        if (profession == null) {
            return VillagerProfession.NONE;
        }

        return BuiltInRegistries.VILLAGER_PROFESSION.get(ResourceLocation.tryParse(profession));
    }

    public QuestType type() {
        if (type == null) {
            throw new RuntimeException("Quest type cannot be empty");
        }

        return QuestType.valueOf(type.toUpperCase(Locale.ROOT));
    }

    private List<Pair<ResourceLocation, Integer>> convertMapsToPairs(Map<String, Integer> list) {
        List<Pair<ResourceLocation, Integer>> pairs = new ArrayList<>();
        for (var entry : list.entrySet()) {
            pairs.add(Pair.of(tryParse(entry.getKey()), entry.getValue()));
        }
        return pairs;
    }

    private ResourceLocation tryParse(String id) {
        var res = new ResourceLocation(id);
        var ns = res.getNamespace();
        var path = res.getPath();
        return ns.equals("minecraft") ? new ResourceLocation(this.namespace, path) : res;
    }
}
