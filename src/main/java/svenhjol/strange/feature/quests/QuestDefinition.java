package svenhjol.strange.feature.quests;

import com.google.gson.Gson;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerProfession;
import svenhjol.strange.feature.quests.battle.BattleDefinition;
import svenhjol.strange.feature.quests.gather.GatherDefinition;
import svenhjol.strange.feature.quests.hunt.HuntDefinition;
import svenhjol.strange.feature.quests.reward.RewardItemDefinition;
import svenhjol.strange.feature.quests.reward.RewardItemFunctionDefinition;
import svenhjol.strange.feature.quests.treasure.TreasureDefinition;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection"})
public class QuestDefinition {
    private String namespace = "minecraft";
    private String id = "";
    private String template = "";
    private ResourceManager manager; // Transient
    private RandomSource random; // Transient
    private long seed; // Cannot be templated
    private boolean epic = false; // Cannot be templated
    private int level = 0;
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
    private int reward_experience = 0;
    private double reward_multiplier = 1.0d;
    private QuestDefinition cachedTemplate = null;

    public static QuestDefinition deserialize(ResourceLocation id, String namespace, ResourceManager manager, Resource resource) throws IOException {
        BufferedReader reader;

        reader = resource.openAsReader();
        var def = new Gson().fromJson(reader, QuestDefinition.class);

        def.id = id.toString(); // This is used to log the definition that a quest uses.
        def.namespace = namespace;
        def.manager = manager;
        def.seed = RandomSource.create().nextLong();
        def.random = RandomSource.create(def.seed);

        return def;
    }

    public String id() {
        return id;
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

    public int level() {
        int fromTemplate = template().map(QuestDefinition::level).orElse(0);

        if (level > 0) {
            return level;
        }

        return fromTemplate;
    }

    public int loyalty() {
        if (Quests.ignoreLoyalty) {
            return 0;
        }

        int fromTemplate = template().map(QuestDefinition::loyalty).orElse(0);

        if (loyalty > 0) {
            return loyalty;
        }

        return fromTemplate;
    }

    public QuestType type() {
        var fromTemplate = template().map(QuestDefinition::type).orElse(null);
        return type != null ? QuestType.valueOf(type.toUpperCase(Locale.ROOT)) : fromTemplate;
    }

    public List<VillagerProfession> professions() {
        List<VillagerProfession> out = template().map(QuestDefinition::professions).orElse(new ArrayList<>());

        for (var id : professions) {
            if (id.equals("villager") || id.equals("none") || id.equals("any")) {
                out.add(VillagerProfession.NONE);
            } else {
                out.add(BuiltInRegistries.VILLAGER_PROFESSION.get(ResourceLocation.tryParse(id)));
            }
        }

        return out;
    }

    public boolean epic() {
        return epic; // Cannot be templated.
    }

    public List<ResourceLocation> requiredFeatures() {
        var fromTemplate = template().map(QuestDefinition::requiredFeatures).orElse(new ArrayList<>());
        fromTemplate.addAll(required_features.stream().map(this::tryParse).toList());
        return fromTemplate;
    }

    public DefinitionList<TreasureDefinition> treasure() {
        var fromTemplate = template().map(QuestDefinition::treasure).orElse(new DefinitionList<>());
        fromTemplate.addAll(treasure.stream()
            .map(m -> new TreasureDefinition(this).fromMap(m))
            .collect(Collectors.toCollection(DefinitionList::new)));
        return fromTemplate;
    }

    public DefinitionList<BattleDefinition> battle() {
        var fromTemplate = template().map(QuestDefinition::battle).orElse(new DefinitionList<>());
        fromTemplate.addAll(battle.stream()
            .map(m -> new BattleDefinition(this).fromMap(m))
            .collect(Collectors.toCollection(DefinitionList::new)));
        return fromTemplate;
    }

    public DefinitionList<GatherDefinition> gather() {
        var fromTemplate = template().map(QuestDefinition::gather).orElse(new DefinitionList<>());
        fromTemplate.addAll(gather.stream()
            .map(m -> new GatherDefinition(this).fromMap(m))
            .collect(Collectors.toCollection(DefinitionList::new)));
        return fromTemplate;
    }

    public DefinitionList<HuntDefinition> hunt() {
        var fromTemplate = template().map(QuestDefinition::hunt).orElse(new DefinitionList<>());
        fromTemplate.addAll(hunt.stream()
            .map(m -> new HuntDefinition(this).fromMap(m))
            .collect(Collectors.toCollection(DefinitionList::new)));
        return fromTemplate;
    }

    public DefinitionList<RewardItemDefinition> rewardItems() {
        var fromTemplate = template().map(QuestDefinition::rewardItems).orElse(new DefinitionList<>());
        fromTemplate.addAll(reward_items.stream()
            .map(m -> new RewardItemDefinition(this).fromMap(m))
            .collect(Collectors.toCollection(DefinitionList::new)));
        return fromTemplate;
    }

    public DefinitionList<RewardItemFunctionDefinition> rewardItemFunctions() {
        var fromTemplate = template().map(QuestDefinition::rewardItemFunctions).orElse(new DefinitionList<>());
        fromTemplate.addAll(reward_item_functions.stream()
            .map(m -> new RewardItemFunctionDefinition(this).fromMap(m))
            .collect(Collectors.toCollection(DefinitionList::new)));
        return fromTemplate;
    }

    public int rewardExperience() {
        var fromTemplate = template().map(QuestDefinition::rewardExperience).orElse(0);
        return reward_experience > 0 ? reward_experience : fromTemplate;
    }

    public double rewardMultiplier() {
        var fromTemplate = template().map(QuestDefinition::rewardMultiplier).orElse(1.0d);
        return reward_multiplier > 1 ? reward_multiplier : fromTemplate;
    }

    private ResourceLocation tryParse(String id) {
        var res = new ResourceLocation(id);
        var ns = res.getNamespace();
        var path = res.getPath();
        return ns.equals("minecraft") ? new ResourceLocation(this.namespace, path) : res;
    }

    private Optional<QuestDefinition> template() {
        if (cachedTemplate == null && !template.isEmpty()) {
            // Lookup from loaded definitions.
            var res = new ResourceLocation(template);
            cachedTemplate = Quests.DEFINITION_TEMPLATES.get(res);
        }

        return Optional.ofNullable(cachedTemplate);
    }
}
