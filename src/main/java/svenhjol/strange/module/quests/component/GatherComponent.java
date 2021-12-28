package svenhjol.strange.module.quests.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import svenhjol.strange.module.quests.IQuestComponent;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.definition.QuestDefinition;
import svenhjol.strange.module.quests.helper.QuestDefinitionHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GatherComponent implements IQuestComponent {
    public static final String ITEMS_TAG = "items";
    public static final String ITEM_DATA_TAG = "item_data";
    public static final String ITEM_COUNT_TAG = "item_count";

    public static final int MAX_ITEMS = 4;

    private final Quest quest;
    private final Map<ItemStack, Integer> items = new HashMap<>();
    private final Map<ItemStack, Integer> satisfied = new HashMap<>(); // dynamic generation, not stored in nbt

    public GatherComponent(Quest quest) {
        this.quest = quest;
    }

    @Override
    public String getId() {
        return "gather";
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public CompoundTag save() {
        CompoundTag outTag = new CompoundTag();
        CompoundTag dataTag = new CompoundTag();
        CompoundTag countTag = new CompoundTag();

        if (items.isEmpty()) return null;

        int index = 0;
        for (ItemStack stack : items.keySet()) {
            String tagIndex = Integer.toString(index);
            int itemCount = items.get(stack);

            // write the data to the tags at the specified index
            CompoundTag itemTag = new CompoundTag();
            stack.save(itemTag);
            dataTag.put(tagIndex, itemTag);
            countTag.putInt(tagIndex, itemCount);

            index++;
        }

        outTag.put(ITEM_DATA_TAG, dataTag);
        outTag.put(ITEM_COUNT_TAG, countTag);
        return outTag;
    }

    @Override
    public void load(CompoundTag nbt) {
        CompoundTag dataTag = (CompoundTag) nbt.get(ITEM_DATA_TAG);
        CompoundTag countTag = (CompoundTag) nbt.get(ITEM_COUNT_TAG);

        items.clear();

        if (dataTag != null && dataTag.size() > 0 && countTag != null) {
            for (int i = 0; i < dataTag.size(); i++) {
                String tagIndex = String.valueOf(i);
                Tag tagAtIndex = dataTag.get(tagIndex);
                if (tagAtIndex == null) continue;

                ItemStack stack = ItemStack.of((CompoundTag)tagAtIndex);
                int count = Math.max(countTag.getInt(tagIndex), 1);
                items.put(stack, count);
            }
        }
    }

    @Override
    public boolean start(Player player) {
        if (player.level.isClientSide) return false;

        QuestDefinition definition = quest.getDefinition();
        Map<String, Map<String, String>> gatherDefinition = definition.getGather().getOrDefault(ITEMS_TAG, null);
        if (gatherDefinition == null || gatherDefinition.isEmpty()) return true;

        GatherComponent gather = quest.getComponent(GatherComponent.class);
        List<ItemStack> items = QuestDefinitionHelper.parseItems((ServerPlayer)player, gatherDefinition, MAX_ITEMS);
        if (items.isEmpty()) return false;

        items.forEach(gather::addItem);
        return true;
    }

    @Override
    public boolean isSatisfied(Player player) {
        if (items.isEmpty()) return true;
        var count = 0;

        for (ItemStack i : items.keySet()) {
            if (satisfied.containsKey(i) && satisfied.get(i) == (int)items.get(i)) {
                count++;
            }
        }

        return items.size() == count;
    }

    @Override
    public void update(Player player) {
        satisfied.clear();

        Map<ItemStack, Integer> itemsCopy = new HashMap<>(items);
        List<ItemStack> invCopy = new ArrayList<>(player.getInventory().items);

        itemsCopy.forEach((requiredStack, requiredCount) -> {
            int removeIndex = -1;
            int sum = 0;

            for (int i = 0; i < invCopy.size(); i++) {
                ItemStack invStack = invCopy.get(i);
                if (invStack.isEmpty()) continue;

                if (requiredStack.sameItem(invStack)) {
                    sum += invStack.getCount();
                    satisfied.put(requiredStack, sum);
                    if (sum >= requiredCount) {
                        satisfied.put(requiredStack, requiredCount);
                        removeIndex = i;
                        break;
                    }
                }
            }

            if (removeIndex >= 0) {
                invCopy.remove(removeIndex);
            }
        });
    }

    @Override
    public void complete(Player player, @Nullable AbstractVillager merchant) {
        if (items.isEmpty()) return;

        // remove the required items from the player's inventory
        items.forEach((stack, count) -> {
            int remainder = count;
            for (ItemStack inv : player.getInventory().items) {
                if (remainder <= 0) continue;

                if (stack.sameItem(inv)) {
                    int decrement = Math.min(remainder, inv.getCount());
                    remainder -= decrement;
                    inv.shrink(decrement);
                }
            }
        });
    }

    public void addItem(ItemStack stack) {
        items.put(stack, stack.getCount());
    }

    public Map<ItemStack, Integer> getItems() {
        return items;
    }

    public Map<ItemStack, Integer> getSatisfied() {
        return satisfied;
    }
}
