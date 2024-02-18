package svenhjol.strange.feature.quests.treasure;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.Strange;
import svenhjol.strange.data.LinkedItemList;
import svenhjol.strange.data.LinkedResourceList;
import svenhjol.strange.feature.quests.QuestDefinition;
import svenhjol.strange.feature.quests.Quests;
import svenhjol.strange.feature.quests.BaseDefinition;

import java.util.*;

public class TreasureDefinition extends BaseDefinition<TreasureDefinition> {
    static final ResourceLocation DEFAULT_TREASURE_ITEMS = new ResourceLocation(Strange.ID, "default_treasure_items");
    private ResourceLocation list;
    private int amount = 1;
    private double chance = 1.0d;
    private List<ResourceLocation> items = new ArrayList<>();

    public TreasureDefinition(QuestDefinition definition) {
        super(definition);
        items.add(DEFAULT_TREASURE_ITEMS);
    }

    @Override
    public TreasureDefinition fromMap(Map<String, Object> map) {
        for (var entry : map.entrySet()) {
            var key = entry.getKey();
            var val = entry.getValue();

            switch (key) {
                case "list": {
                    this.list = parseResourceLocation(val).orElseThrow();
                    break;
                }
                case "amount": {
                    this.amount = parseInteger(val).orElseThrow();
                    break;
                }
                case "items": {
                    this.items = parseResourceLocationList(val).orElseThrow();
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
        return "quests/treasure";
    }

    public double chance() {
        return chance;
    }

    public ItemStack item() {
        if (items.isEmpty()) {
            throw new RuntimeException("Treasure item list is empty: " + items);
        }

        var copy = new ArrayList<>(items);
        Util.shuffle(copy, random());

        var items = LinkedItemList.load(entries().getOrDefault(copy.get(0), new LinkedList<>()));
        if (items.isEmpty()) {
            throw new RuntimeException("Treasure item list is empty: " + items);
        }

        Util.shuffle(items, random());
        return new ItemStack(items.get(0));
    }

    public List<ResourceLocation> lootTables() {
        List<ResourceLocation> out = new ArrayList<>();

        var lootTables = LinkedResourceList.load(entries().getOrDefault(list, new LinkedList<>()));
        if (lootTables.isEmpty()) {
            throw new RuntimeException("Treasure loot table list is empty: " + list);
        }

        Util.shuffle(lootTables, random());

        var max = Math.min(Math.min(Quests.maxQuestRequirements, amount), lootTables.size());
        for (int i = 0; i < max; i++) {
            out.add(lootTables.get(i));
        }

        return out;
    }
}
