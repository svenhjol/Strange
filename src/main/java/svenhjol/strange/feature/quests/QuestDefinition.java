package svenhjol.strange.feature.quests;

import com.google.gson.Gson;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerProfession;
import svenhjol.strange.feature.quests.treasure.TreasureDefinition;
import svenhjol.strange.feature.quests.battle.BattleDefinition;
import svenhjol.strange.feature.quests.gather.GatherDefinition;
import svenhjol.strange.feature.quests.hunt.HuntDefinition;
import svenhjol.strange.feature.quests.reward.RewardItemDefinition;
import svenhjol.strange.feature.quests.reward.RewardItemFunctionDefinition;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection"})
public class QuestDefinition {
    private String namespace = "minecraft";
    private ResourceManager manager; // Transient
    private RandomSource random; // Transient
    private long seed;
    private boolean epic = false;
    private int level = 1;
    private int loyalty = 0;
    private String type;
    private List<String> professions = new ArrayList<>();
    private List<String> required_features = new ArrayList<>();
    private List<Map<String, Object>> treasure = new ArrayList<>();
    private List<Map<String, Object>> battle = new ArrayList<>();
    private List<Map<String, Object>> gather = new ArrayList<>();
    private List<Map<String, Object>> hunt = new ArrayList<>();
    private List<Map<String, Object>> reward_items = new ArrayList<>();
    private List<Map<String, Object>> reward_item_functions = new ArrayList<>();
    private int reward_experience = 1;

    public static QuestDefinition deserialize(String namespace, ResourceManager manager, Resource resource) throws IOException {
        BufferedReader reader;

        reader = resource.openAsReader();
        var def = new Gson().fromJson(reader, QuestDefinition.class);

        def.namespace = namespace;
        def.manager = manager;
        def.seed = RandomSource.create().nextLong();
        def.random = RandomSource.create(def.seed);

        return def;
    }

    public String namespace() {
        return namespace;
    }

    public ResourceManager manager() {
        return manager;
    }

    public long seed() {
        return seed;
    }

    public RandomSource random() {
        if (random == null) {
            random = RandomSource.create(seed);
        }
        return random;
    }

    public Pair<ResourceLocation, Integer> pair(List<Pair<ResourceLocation, Integer>> list, RandomSource random) {
        return list.get(random.nextInt(list.size()));
    }

    public List<ResourceLocation> requiredFeatures() {
        return required_features.stream().map(this::tryParse).toList();
    }

    public DefinitionList<TreasureDefinition> treasure() {
        return treasure.stream()
            .map(m -> new TreasureDefinition(this).fromMap(m))
            .collect(Collectors.toCollection(DefinitionList::new));
    }

    public DefinitionList<BattleDefinition> battle() {
        return battle.stream()
            .map(m -> new BattleDefinition(this).fromMap(m))
            .collect(Collectors.toCollection(DefinitionList::new));
    }

    public DefinitionList<GatherDefinition> gather() {
        return gather.stream()
            .map(m -> new GatherDefinition(this).fromMap(m))
            .collect(Collectors.toCollection(DefinitionList::new));
    }

    public DefinitionList<HuntDefinition> hunt() {
        return hunt.stream()
            .map(m -> new HuntDefinition(this).fromMap(m))
            .collect(Collectors.toCollection(DefinitionList::new));
    }

    public DefinitionList<RewardItemDefinition> rewardItems() {
        return reward_items.stream()
            .map(m -> new RewardItemDefinition(this).fromMap(m))
            .collect(Collectors.toCollection(DefinitionList::new));
    }

    public DefinitionList<RewardItemFunctionDefinition> rewardItemFunctions() {
        return reward_item_functions.stream()
            .map(m -> new RewardItemFunctionDefinition(this).fromMap(m))
            .collect(Collectors.toCollection(DefinitionList::new));
    }

    public int rewardExperience() {
        return reward_experience == 1 ? level() * level() : reward_experience;
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

    public List<VillagerProfession> professions() {
        List<VillagerProfession> out = new ArrayList<>();

        for (var id : professions) {
            if (id.equals("villager") || id.equals("none") || id.equals("any")) {
                out.add(VillagerProfession.NONE);
            } else {
                out.add(BuiltInRegistries.VILLAGER_PROFESSION.get(ResourceLocation.tryParse(id)));
            }
        }

        return out;
    }

    public QuestType type() {
        if (type == null) {
            throw new RuntimeException("Quest type cannot be empty");
        }

        return QuestType.valueOf(type.toUpperCase(Locale.ROOT));
    }

    private ResourceLocation tryParse(String id) {
        var res = new ResourceLocation(id);
        var ns = res.getNamespace();
        var path = res.getPath();
        return ns.equals("minecraft") ? new ResourceLocation(this.namespace, path) : res;
    }
}
