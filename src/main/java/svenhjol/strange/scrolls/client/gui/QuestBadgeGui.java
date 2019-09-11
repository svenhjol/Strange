package svenhjol.strange.scrolls.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.strange.base.message.RequestShowQuest;
import svenhjol.strange.scrolls.quest.iface.IQuest;

public class QuestBadgeGui extends AbstractGui
{
    private int x0, x1, y0, y1;
    private IQuest quest;
    private Minecraft mc;

    public QuestBadgeGui(IQuest quest, int x, int y)
    {
        this.quest = quest;
        this.mc = Minecraft.getInstance();

        x0 = 10;
        x1 = 110;
        y0 = y;
        y1 = y + 24;

        AbstractGui.fill(x0, y0, x1, y1, 0x88000000);
        drawCenteredString(mc.fontRenderer, quest.getTitle(), 60, y + 6, 0xFFFFFF);

        // progress
        float completion = quest.getCriteria().getCompletion();
        boolean isComplete = quest.getCriteria().isSatisfied();

        int color = isComplete ? 0x8800FF00 : 0x88FFFF00;

        AbstractGui.fill(x0, y1-3, x1, y1, 0x88333333);
        AbstractGui.fill(x0, y1-3, x0 + (int)completion, y1, color);
    }

    public boolean isInBox(double x, double y)
    {
        return x >= x0 && x <= x1 && y >= y0 && y <= y1;
    }

    public void onLeftClick()
    {
        PacketHandler.sendToServer(new RequestShowQuest(quest.getId()));
    }
}
