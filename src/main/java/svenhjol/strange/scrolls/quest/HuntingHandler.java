package svenhjol.strange.scrolls.quest;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import svenhjol.meson.Meson;

public class HuntingHandler implements IQuestHandler
{
    @Override
    public void pickupItem(IQuest quest, PlayerEntity player, ItemStack stack)
    {
        // no op
    }

    @Override
    public void killMob(IQuest quest, PlayerEntity player, LivingEntity mob)
    {
        Meson.log("Player killed " + mob);
    }
}
