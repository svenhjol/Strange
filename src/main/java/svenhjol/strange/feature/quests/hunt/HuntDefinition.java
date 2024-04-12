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
    private EntityType<?> entity;
    private int amount = 1;
    private double weight = -1;
    private List<ResourceLocation> lists = new ArrayList<>();

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
                    var list = parseResourceLocation(val).orElseThrow();
                    this.lists.add(list);
                    break;
                }
                case "lists": {
                    this.lists = parseResourceLocationList(val).orElseThrow();
                    break;
                }
                case "mob", "entity": {
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

        // Handle case when single mob.
        if (entity != null) {
            out.add(Pair.of(entity, amount));
            return out;
        }

        // Handle case when list or lists.
        if (!lists.isEmpty()) {
            LinkedEntityTypeList allEntities = new LinkedEntityTypeList();
            LinkedEntityTypeList limitedEntities = new LinkedEntityTypeList();

            for (var list : lists) {
                var mobs = LinkedEntityTypeList.load(entries().getOrDefault(list, new LinkedList<>()));
                if (mobs.isEmpty()) {
                    throw new RuntimeException("Hunt entity list is empty: " + list + " in " + definition.id());
                }
                allEntities.addAll(mobs);
            }

            Util.shuffle(allEntities, random());

            var max = Math.min(
                Math.min(Quests.maxQuestRequirements, allEntities.size()),
                definition.level() + random().nextInt(2));

            if (weight <= 0) {
                limitedEntities.addAll(allEntities.subset(max, random()));
            } else {
                limitedEntities.addAll(allEntities.subset(max, weight, 0.1d, random()));
            }

            for (var mob : limitedEntities) {
                out.add(Pair.of(mob, amount / max));
            }
            return out;
        }

        throw new RuntimeException("Hunt items failed to populate: " + definition.id());
    }
}
