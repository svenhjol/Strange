package svenhjol.strange.scrolls.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import svenhjol.meson.Meson;
import svenhjol.strange.scrolls.quest.IQuest;

public class QuestBadgeGui extends AbstractGui
{
    private int x0, x1, y0, y1;
    private IQuest quest;

    public QuestBadgeGui(IQuest quest, Minecraft mc, int x, int y)
    {
        this.quest = quest;

        x0 = 10;
        x1 = 110;
        y0 = y;
        y1 = y + 20;

        AbstractGui.fill(x0, y0, x1, y1, 0x44000000);
        drawCenteredString(mc.fontRenderer, quest.getType().toString(), 60, y + 6, 0xffffff);
    }

    public boolean isInBox(double x, double y)
    {
        return x >= x0 && x <= x1 && y >= y0 && y <= y1;
    }

    public void onLeftClick()
    {
        Meson.log(quest);
    }
}
