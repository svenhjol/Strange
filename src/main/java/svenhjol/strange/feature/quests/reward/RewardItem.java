package svenhjol.strange.feature.quests.reward;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.QuestsHelper;
import svenhjol.strange.feature.quests.Reward;

import java.util.List;

public class RewardItem implements Reward {
    static final String STACK_TAG = "stack";

    public ItemStack stack;
    public Quest quest;

    public RewardItem() {
    }

    public RewardItem(ItemStack stack) {
        this.stack = stack;
    }

    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    @Override
    public Quest.RewardType type() {
        return Quest.RewardType.ITEM;
    }

    @Override
    public void start() {
        // no op
    }

    @Override
    public void complete() {
        if (quest == null) return;

        var player = quest.player();
        if (player == null) return;

        QuestsHelper.getNearbyMatchingVillager(player.level(), player.blockPosition(), quest.villagerUuid()).ifPresent(
            villager -> QuestsHelper.throwItemsAtPlayer(villager, player, List.of(stack)));
    }

    @Override
    public void load(CompoundTag tag) {
        stack = ItemStack.of(tag.getCompound(STACK_TAG));
    }

    @Override
    public void save(CompoundTag tag) {
        var itemTag = new CompoundTag();
        stack.save(itemTag);
        tag.put(STACK_TAG, itemTag);
    }
}
