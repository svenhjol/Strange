package svenhjol.strange.scrolls.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import svenhjol.meson.helper.ClientHelper;
import svenhjol.strange.scrolls.client.screen.QuestScreen;
import svenhjol.strange.scrolls.client.screen.ScrollScreen;
import svenhjol.strange.scrolls.client.toast.QuestToast;
import svenhjol.strange.scrolls.client.toast.QuestToastTypes;
import svenhjol.strange.scrolls.quest.iface.IQuest;

@OnlyIn(Dist.CLIENT)
public class QuestProxyClient implements IQuestProxy
{
    @Override
    public void showQuest(IQuest quest)
    {
        PlayerEntity player = ClientHelper.getClientPlayer();
        Minecraft.getInstance().displayGuiScreen(new QuestScreen(player, quest));
    }

    @Override
    public void showScroll(Hand hand)
    {
        PlayerEntity player = ClientHelper.getClientPlayer();
        Minecraft.getInstance().displayGuiScreen(new ScrollScreen(player, hand));
    }

    @Override
    public void toast(IQuest quest, QuestToastTypes.Type type, String title)
    {
        Minecraft.getInstance().getToastGui().add(new QuestToast(quest, type, title, quest.getTitle()));
    }
}
