package svenhjol.strange.feature.quests.hunt;

import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import svenhjol.strange.data.LinkedEntityTypeList;
import svenhjol.strange.feature.quests.QuestDefinition;
import svenhjol.strange.feature.quests.Quests;
import svenhjol.strange.feature.quests.BaseDefinition;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HuntDefinition extends BaseDefinition<HuntDefinition> {
    private ResourceLocation list;
    private int amount = 1;
    private double weight = -1;

    public HuntDefinition(QuestDefinition definition) {
        super(definition);
    }

    @Override
    public HuntDefinition fromMap(Map<String, Object> map) {
        for (var entry : map.entrySet()) {
            var key = entry.getKey();
            var val = entry.getValue();

            switch (key) {
                case "list": {
                    this.list = parseResourceLocation(val);
                    break;
                }
                case "amount": {
                    this.amount = parseInteger(val);
                    break;
                }
                case "weight": {
                    this.weight = parseDouble(val);
                    break;
                }
            }
        }

        return this;
    }

    @Override
    protected String dataDir() {
        return "quests/hunt";
    }

    public List<Pair<EntityType<?>, Integer>> mobs() {
        List<Pair<EntityType<?>, Integer>> out = new ArrayList<>();
        List<EntityType<?>> sublist = new ArrayList<>();

        var mobs = LinkedEntityTypeList.load(entries().getOrDefault(list, new LinkedList<>()));
        if (mobs.isEmpty()) {
            throw new RuntimeException("Entity list is empty");
        }

        Util.shuffle(mobs, random());

        var max = Math.min(
            Math.min(Quests.maxQuestRequirements, amount),
            definition.level() + random().nextInt(2));

        if (weight < 0) {
            sublist.addAll(mobs.subset(max, random()));
        } else {
            sublist.addAll(mobs.subset(max, weight, 0.1d, random()));
        }

        for (var mob : sublist) {
            out.add(Pair.of(mob, amount / max));
        }

        return out;
    }
}
