package svenhjol.strange.feature.quests.reward;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.Reward;

public class RewardExperience implements Reward {
    static final String TOTAL_TAG = "total";
    public int total;
    public Quest quest;

    public RewardExperience(Quest quest) {
        this.quest = quest;
    }

    public RewardExperience(Quest quest, int total) {
        this.quest = quest;
        this.total = total;
    }

    @Override
    public Quest.RewardType type() {
        return Quest.RewardType.EXPERIENCE_LEVEL;
    }

    @Override
    public void start() {
        // no op
    }

    @Override
    public void complete() {
        var player = quest.player();
        if (player == null) return;

        player.giveExperienceLevels(total);
        player.level().playLocalSound(player, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void load(CompoundTag tag) {
        total = tag.getInt(TOTAL_TAG);
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putInt(TOTAL_TAG, total);
    }
}
