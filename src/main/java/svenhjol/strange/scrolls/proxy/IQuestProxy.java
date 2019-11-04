package svenhjol.strange.scrolls.proxy;

import net.minecraft.util.Hand;
import svenhjol.strange.scrolls.client.toast.QuestToastTypes;
import svenhjol.strange.scrolls.quest.iface.IQuest;

public interface IQuestProxy
{
    void showScroll(Hand hand);

    void showQuest(IQuest quest);

    void toast(IQuest quest, QuestToastTypes.Type type, String title);
}
