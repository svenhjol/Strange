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
import svenhjol.strange.module.quests.QuestDefinition;
import svenhjol.strange.module.quests.QuestDefinitionHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GatherComponent implements IQuestComponent {
    public static final String TAG_ITEMS = "items";
    public static final String TAG_ITEM_DATA = "item_data";
    public static final String TAG_ITEM_COUNT = "item_count";

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
    public CompoundTag toNbt() {
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

        outTag.put(TAG_ITEM_DATA, dataTag);
        outTag.put(TAG_ITEM_COUNT, countTag);
        return outTag;
    }

    @Override
    public void fromNbt(CompoundTag nbt) {
        CompoundTag dataTag = (CompoundTag) nbt.get(TAG_ITEM_DATA);
        CompoundTag countTag = (CompoundTag) nbt.get(TAG_ITEM_COUNT);

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
        Map<String, Map<String, String>> gatherDefinition = definition.getGather().getOrDefault(TAG_ITEMS, null);
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
            for (ItemStack invStack : player.getInventory().items) {
                if (remainder <= 0) continue;

                if (stack.sameItem(invStack)) {
                    int decrement = Math.min(remainder, invStack.getCount());
                    remainder -= decrement;
                    invStack.shrink(decrement);
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
