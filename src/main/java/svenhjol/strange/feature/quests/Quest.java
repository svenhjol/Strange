package svenhjol.strange.feature.quests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Quest {
    protected IQuestDefinition definition;
    protected RandomSource random;
    protected Map<ResourceLocation, Integer> required = new HashMap<>();
    protected Map<ResourceLocation, Integer> completed = new HashMap<>();

    public Quest(IQuestDefinition definition) {
        this.random = RandomSource.create();
        this.definition = definition;

        var type = definition.type();
        var pool = definition.randomPool(random);
        var values = new ArrayList<>(type.tagValueIds(pool));
        var amount = random.nextInt(2) + 10;
        var selection = Math.min(values.size(), random.nextInt(2) + 1);

        Collections.shuffle(values);
        for (int i = 0; i < selection; i++) {
            var requirement = values.get(i);
            required.put(requirement, amount / selection);
            completed.put(requirement, 0);
        }
    }
}
