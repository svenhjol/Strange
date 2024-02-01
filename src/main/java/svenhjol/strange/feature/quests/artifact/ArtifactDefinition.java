package svenhjol.strange.feature.quests.artifact;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.Strange;
import svenhjol.strange.data.LinkedItemList;
import svenhjol.strange.data.LinkedResourceList;
import svenhjol.strange.feature.quests.QuestDefinition;
import svenhjol.strange.feature.quests.Quests;
import svenhjol.strange.feature.quests.BaseDefinition;
import svenhjol.strange.feature.quests.artifact.ArtifactQuest.Method;

import java.util.*;

public class ArtifactDefinition extends BaseDefinition<ArtifactDefinition> {
    static final ResourceLocation DEFAULT_ARTIFACT_ITEMS = new ResourceLocation(Strange.ID, "default_artifact_items");
    private ResourceLocation list;
    private Method method = Method.AND;
    private int amount = 1;
    private double chance = 1.0d;
    private List<ResourceLocation> items = new ArrayList<>();

    public ArtifactDefinition(QuestDefinition definition) {
        super(definition);
        items.add(DEFAULT_ARTIFACT_ITEMS);
    }

    @Override
    public ArtifactDefinition fromMap(Map<String, Object> map) {
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
                case "method": {
                    this.method = Method.valueOf(((String)val).toUpperCase(Locale.ROOT));
                    break;
                }
                case "items": {
                    this.items = parseResourceLocationList(val);
                    break;
                }
                case "chance": {
                    this.chance = parseDouble(val);
                    break;
                }
            }
        }

        return this;
    }

    @Override
    protected String dataDir() {
        return "quests/artifact";
    }

    public Method method() {
        return method;
    }

    public double chance() {
        return chance;
    }

    public ItemStack item() {
        if (items.isEmpty()) {
            throw new RuntimeException("Items list is empty");
        }

        var copy = new ArrayList<>(items);
        Util.shuffle(copy, random());

        var items = LinkedItemList.load(entries().getOrDefault(copy.get(0), new LinkedList<>()));
        if (items.isEmpty()) {
            throw new RuntimeException("Items list is empty");
        }

        Util.shuffle(items, random());
        return new ItemStack(items.get(0));
    }

    public List<ResourceLocation> lootTables() {
        List<ResourceLocation> out = new ArrayList<>();

        var lootTables = LinkedResourceList.load(entries().getOrDefault(list, new LinkedList<>()));
        if (lootTables.isEmpty()) {
            throw new RuntimeException("Loot table list is empty");
        }

        Util.shuffle(lootTables, random());

        int max = switch (method) {
            case AND -> Math.min(Math.min(Quests.maxQuestRequirements, amount), lootTables.size());
            case OR -> 1;
        };

        for (int i = 0; i < max; i++) {
            out.add(lootTables.get(i));
        }

        return out;
    }
}
