package svenhjol.strange.feature.quests.hunt;

import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import svenhjol.strange.data.LinkedEntityTypeList;
import svenhjol.strange.feature.quests.BaseDefinition;
import svenhjol.strange.feature.quests.QuestDefinition;
import svenhjol.strange.feature.quests.Quests;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HuntDefinition extends BaseDefinition<HuntDefinition> {
    private ResourceLocation list;
    private EntityType<?> entity;
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
                    this.list = parseResourceLocation(val).orElseThrow();
                    break;
                }
                case "entity": {
                    this.entity = parseEntity(val).orElseThrow();
                    break;
                }
                case "amount": {
                    this.amount = parseInteger(val).orElseThrow();
                    break;
                }
                case "weight": {
                    this.weight = parseDouble(val).orElseThrow();
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

        var localAmount = amount;
        var localMax = Quests.maxQuestRequirements;

        if (entity != null && list != null) {
            localAmount /= 2;
            localMax -= 1;
        }

        if (entity != null) {
            out.add(Pair.of(entity, localAmount));
        }

        if (list != null) {
            var mobs = LinkedEntityTypeList.load(entries().getOrDefault(list, new LinkedList<>()));
            if (mobs.isEmpty()) {
                throw new RuntimeException("Hunt entity list is empty: " + list);
            }

            Util.shuffle(mobs, random());

            var max = Math.min(
                Math.min(localMax, mobs.size()),
                definition.level() + random().nextInt(2));

            if (weight < 0) {
                sublist.addAll(mobs.subset(max, random()));
            } else {
                sublist.addAll(mobs.subset(max, weight, 0.1d, random()));
            }

            for (var mob : sublist) {
                out.add(Pair.of(mob, localAmount / max));
            }
        }

        return out;
    }
}
