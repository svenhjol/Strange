package svenhjol.strange.scrolls.proxy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import svenhjol.strange.scrolls.quest.iface.IQuest;
import svenhjol.strange.scrolls.quest.iface.IQuestProxy;

public class QuestProxyServer implements IQuestProxy
{
    @Override
    public void showQuestScreen(PlayerEntity player, IQuest quest)
    {

    }

    @Override
    public void showQuestScreen(PlayerEntity player, ItemStack stack)
    {

    }

    @Override
    public void toast(IQuest quest, String title)
    {

    }

    @Override
    public void toastSuccess(IQuest quest, String title)
    {

    }

    @Override
    public void toastFailed(IQuest quest, String title)
    {

    }
}
