package svenhjol.strange.feature.quests.reward;

import svenhjol.strange.feature.quests.BaseDefinition;
import svenhjol.strange.feature.quests.QuestDefinition;
import svenhjol.strange.feature.quests.Quests;

import java.util.Map;

public class RewardItemFunctionDefinition extends BaseDefinition<RewardItemFunctionDefinition> {
    private String name;
    private final RewardItemFunctionParameters parameters;

    public RewardItemFunctionDefinition(QuestDefinition definition) {
        super(definition);
        this.parameters = new RewardItemFunctionParameters(this);
    }

    @Override
    public RewardItemFunctionDefinition fromMap(Map<String, Object> map) {
        name = map.containsKey("name") ? (String)map.get("name") : "";

        if (name.isEmpty()) {
            throw new RuntimeException("Missing function name");
        }

        for (var entry : map.entrySet()) {
            var key = entry.getKey();
            var val = entry.getValue();

            if (key.equals("name")) {
                continue; // Already processed
            }

            parameters.add(key, val);
        }

        return this;
    }

    @Override
    protected String dataDir() {
        return "quests/reward";
    }

    public RewardItemFunction function() {
        return Quests.REWARD_ITEM_FUNCTIONS.byId(name, parameters);
    }
}
