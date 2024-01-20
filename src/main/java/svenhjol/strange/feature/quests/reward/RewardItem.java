package svenhjol.strange.feature.quests.reward;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.QuestHelper;
import svenhjol.strange.feature.quests.Reward;

import java.util.List;

public class RewardItem implements Reward {
    static final String ITEM_TAG = "item";

    public ItemStack item;
    public Quest<?> quest;

    public RewardItem(Quest<?> quest) {
        this.quest = quest;
    }

    public RewardItem(Quest<?> quest, ItemStack item) {
        this.quest = quest;
        this.item = item;
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
        var player = quest.player();
        if (player == null) return;

        QuestHelper.getNearbyMatchingVillager(player.level(), player.blockPosition(), quest.villagerUuid()).ifPresent(
            villager -> QuestHelper.throwItemsAtPlayer(villager, player, List.of(item)));
    }

    @Override
    public void load(CompoundTag tag) {
        item = ItemStack.of(tag.getCompound(ITEM_TAG));
    }

    @Override
    public void save(CompoundTag tag) {
        var itemTag = new CompoundTag();
        item.save(itemTag);
        tag.put(ITEM_TAG, itemTag);
    }
}
