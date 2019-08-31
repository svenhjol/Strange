package svenhjol.strange.scrolls.quest;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface IQuestHandler
{
    void pickupItem(IQuest quest, PlayerEntity player, ItemStack stack);

    void killMob(IQuest quest, PlayerEntity player, LivingEntity mob);
}
