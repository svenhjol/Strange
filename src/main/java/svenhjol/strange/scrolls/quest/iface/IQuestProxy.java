package svenhjol.strange.scrolls.quest.iface;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface IQuestProxy
{
    void showQuestScreen(PlayerEntity player, IQuest quest);

    void showQuestScreen(PlayerEntity player, ItemStack stack);

    void toast(IQuest quest, String title);

    void toastSuccess(IQuest quest, String title);

    void toastFailed(IQuest quest, String title);
}
