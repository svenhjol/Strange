package svenhjol.strange.scrolls.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import svenhjol.strange.scrolls.client.screen.QuestScreen;
import svenhjol.strange.scrolls.client.toast.QuestToast;
import svenhjol.strange.scrolls.client.toast.QuestToast.Type;
import svenhjol.strange.scrolls.quest.iface.IQuest;
import svenhjol.strange.scrolls.quest.iface.IQuestProxy;

@OnlyIn(Dist.CLIENT)
public class QuestProxyClient implements IQuestProxy
{
    @Override
    public void showQuestScreen(PlayerEntity player, IQuest quest)
    {
        Minecraft.getInstance().displayGuiScreen(new QuestScreen(player, quest));
    }

    @Override
    public void showQuestScreen(PlayerEntity player, ItemStack stack)
    {
        Minecraft.getInstance().displayGuiScreen(new QuestScreen(player, stack));
    }

    @Override
    public void toast(IQuest quest, String title)
    {
        Minecraft.getInstance().getToastGui().add(new QuestToast(quest, Type.General, title, quest.getTitle()));
    }

    @Override
    public void toastSuccess(IQuest quest, String title)
    {
        Minecraft.getInstance().getToastGui().add(new QuestToast(quest, Type.Success, title, quest.getTitle()));
    }

    @Override
    public void toastFailed(IQuest quest, String title)
    {
        Minecraft.getInstance().getToastGui().add(new QuestToast(quest, Type.Failed, title, quest.getTitle()));
    }
}
