package svenhjol.strange.feature.quests.reward;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.data.LinkedItemList;
import svenhjol.strange.feature.quests.QuestDefinition;
import svenhjol.strange.feature.quests.BaseDefinition;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RewardItemDefinition extends BaseDefinition<RewardItemDefinition> {
    public ResourceLocation list;
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
                    this.list = parseResourceLocation(val).orElseThrow();
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
        var max = 4;
        var localAmount = amount;
        var multiplier = definition.rewardMultiplier();

        List<ItemStack> out = new ArrayList<>();

        if (random().nextDouble() > chance) {
            return List.of();
        }

        // TODO: loot tables

        if (item != null && list != null) {
            localAmount /= 2; // Half the full amount goes each of the item and the list
            max -= 1; // One is now the item, the rest is the list
        }

        if (item != null) {
            out.add(new ItemStack(item, localAmount));
        }

        if (list != null) {
            List<Item> sublist = new ArrayList<>();
            var items = LinkedItemList.load(entries().getOrDefault(list, new LinkedList<>()));
            if (items.isEmpty()) {
                throw new RuntimeException("Reward item list is empty: " + list);
            }

            if (weight < 0) {
                sublist.addAll(items.subset(max, random()));
            } else {
                sublist.addAll(items.subset(max, weight, 0.1d, random()));
            }

            for (var item : sublist) {
                out.add(new ItemStack(item, Math.min(item.getMaxStackSize(), (int)(localAmount * multiplier))));
            }
        }

        return out.stream().map(RewardItem::new).toList();
    }
}
