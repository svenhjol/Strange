package svenhjol.strange.feature.quests.gather;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.data.LinkedItemList;
import svenhjol.strange.feature.quests.QuestDefinition;
import svenhjol.strange.feature.quests.Quests;
import svenhjol.strange.feature.quests.BaseDefinition;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GatherDefinition extends BaseDefinition<GatherDefinition> {
    private ResourceLocation list;
    private int amount = 1;
    private double weight = -1;

    public GatherDefinition(QuestDefinition definition) {
        super(definition);
    }

    @Override
    public GatherDefinition fromMap(Map<String, Object> map) {
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
        return "quests/gather";
    }

    public List<Pair<ItemStack, Integer>> items() {
        List<Pair<ItemStack, Integer>> out = new ArrayList<>();
        List<Item> sublist = new ArrayList<>();

        var items = LinkedItemList.load(entries().getOrDefault(list, new LinkedList<>()));
        if (items.isEmpty()) {
            throw new RuntimeException("Gather item list is empty: " + list);
        }

        var max = Math.min(
            Math.min(Quests.maxQuestRequirements, items.size()),
            definition.level() + random().nextInt(2));

        if (weight < 0) {
            sublist.addAll(items.subset(max, random()));
        } else {
            sublist.addAll(items.subset(max, weight, 0.1d, random()));
        }

        for (var item : sublist) {
            out.add(Pair.of(new ItemStack(item), amount / max));
        }

        return out;
    }
}
