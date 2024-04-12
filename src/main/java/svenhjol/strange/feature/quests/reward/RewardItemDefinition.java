package svenhjol.strange.feature.quests.reward;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.data.LinkedItemList;
import svenhjol.strange.feature.quests.BaseDefinition;
import svenhjol.strange.feature.quests.QuestDefinition;
import svenhjol.strange.feature.quests.Quests;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RewardItemDefinition extends BaseDefinition<RewardItemDefinition> {
    public List<ResourceLocation> lists = new ArrayList<>();
    public ResourceLocation lootTable;
    public Item item;
    public int amount;
    public double weight = -1;
    public double chance = 1.0d;

    public RewardItemDefinition(QuestDefinition definition) {
        super(definition);
    }

    @Override
    public RewardItemDefinition fromMap(Map<String, Object> map) {
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
                case "loot_table": {
                    this.lootTable = parseResourceLocation(val).orElseThrow();
                    break;
                }
                case "amount": {
                    this.amount = parseInteger(val).orElseThrow();
                    break;
                }
                case "weight": {
                    this.weight = parseInteger(val).orElseThrow();
                    break;
                }
                case "chance": {
                    this.chance = parseDouble(val).orElseThrow();
                    break;
                }
            }
        }

        return this;
    }

    @Override
    protected String dataDir() {
        return "quests/reward";
    }

    public List<RewardItem> items() {
        var multiplier = definition.rewardMultiplier();

        List<ItemStack> out = new ArrayList<>();

        if (random().nextDouble() > chance) {
            return List.of();
        }

        // TODO: loot tables

        // Handle case when single item type.
        if (item != null) {
            out.add(new ItemStack(item, amount));
        }

        // Handle case when list or lists.
        if (!lists.isEmpty()) {
            LinkedItemList allItems = new LinkedItemList();
            LinkedItemList limitedItems = new LinkedItemList();

            for (var list : lists) {
                var items = LinkedItemList.load(entries().getOrDefault(list, new LinkedList<>()));
                if (items.isEmpty()) {
                    throw new RuntimeException("Reward item list is empty: " + list + " in " + definition.id());
                }
                allItems.addAll(items);
            }

            Util.shuffle(allItems, random());

            if (weight <= 0) {
                limitedItems.addAll(allItems.subset(Quests.maxQuestRewards, random()));
            } else {
                limitedItems.addAll(allItems.subset(Quests.maxQuestRewards, weight, 0.1d, random()));
            }

            for (var item : limitedItems) {
                var count = Math.min(item.getMaxStackSize(), (int)(amount * multiplier));
                out.add(new ItemStack(item, count));
            }
        }

        return out.stream().map(RewardItem::new).toList();
    }
}
