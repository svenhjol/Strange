package svenhjol.strange.feature.quests.gather;

import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
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
    private List<ResourceLocation> lists = new ArrayList<>();
    private Item item;
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
                    var list = parseResourceLocation(val).orElseThrow();
                    this.lists.add(list);
                    break;
                }
                case "lists": {
                    this.lists = parseResourceLocationList(val).orElseThrow();
                    break;
                }
                case "item": {
                    this.item = parseItem(val).orElseThrow();
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
        return "quests/gather";
    }

    public List<Pair<ItemStack, Integer>> items() {
        List<Pair<ItemStack, Integer>> out = new ArrayList<>();

        // Handle case when single item type.
        if (item != null) {
            out.add(Pair.of(new ItemStack(item), amount));
            return out;
        }

        // Handle case when list or lists.
        if (!lists.isEmpty()) {
            LinkedItemList allItems = new LinkedItemList();
            LinkedItemList limitedItems = new LinkedItemList();

            for (var list : lists) {
                var items = LinkedItemList.load(entries().getOrDefault(list, new LinkedList<>()));
                if (items.isEmpty()) {
                    throw new RuntimeException("Gather item list is empty: " + list + " in " + definition.id());
                }
                allItems.addAll(items);
            }

            Util.shuffle(allItems, random());

            var max = Math.min(
                Math.min(Quests.maxQuestRequirements, allItems.size()),
                definition.level() + random().nextInt(2));

            if (weight <= 0) {
                limitedItems.addAll(allItems.subset(max, random()));
            } else {
                limitedItems.addAll(allItems.subset(max, weight, 0.1d, random()));
            }

            for (Item item : limitedItems) {
                out.add(Pair.of(new ItemStack(item), amount / max));
            }
            return out;
        }

        throw new RuntimeException("Gather items failed to populate: " + definition.id());
    }
}
