package svenhjol.strange.scrolls.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.resources.I18n;
import svenhjol.meson.Meson;
import svenhjol.strange.scrolls.Quests;
import svenhjol.strange.scrolls.quest.iface.IQuest;

public class QuestBadgeGui extends AbstractGui
{
    private int x0, x1, y0, y1;
    private int buttonX0, buttonY0, buttonX1, buttonY1;
    private IQuest quest;
    private Minecraft mc;
    public static int WIDTH = 100;
    public static int HEIGHT = 14;
    public static int BUTTON_PADDING = 2;

    public QuestBadgeGui(IQuest quest, int x, int y)
    {
        this.quest = quest;
        this.mc = Minecraft.getInstance();

        x0 = x;
        x1 = x + WIDTH;
        y0 = y;
        y1 = y + HEIGHT;

        // Build button background
        this.buildButton(x0 - BUTTON_PADDING, y0 - BUTTON_PADDING, WIDTH + BUTTON_PADDING*2, HEIGHT + BUTTON_PADDING*2);

//        AbstractGui.fill(x0, y0, x1, y1, 0x88000000);
        drawCenteredString(mc.fontRenderer, I18n.format(quest.getTitle()), x + 50, y, 0xFFFFFF);

        // progress
        float completion = quest.getCriteria().getCompletion();
        boolean isComplete = quest.getCriteria().isSatisfied();

        int color = isComplete ? 0x8800FF00 : 0x88FFFF00;

        AbstractGui.fill(x0, y1-3, x1, y1, 0x88333333);
        AbstractGui.fill(x0, y1-3, x0 + (int)completion, y1, color);
    }

    public void buildButton(int butX, int butY, int butWidth, int butHeight)
    {
        buttonX0 = butX;
        buttonX1 = butX + butWidth;
        buttonY0 = butY;
        buttonY1 = butY + butHeight;

        AbstractGui.fill(buttonX0, buttonY0, buttonX1, buttonY1, 0x88AAAAAA);
        AbstractGui.fill(buttonX0 - 1, buttonY0 - 1, buttonX1 + 1, buttonY0, 0x88FFFFFF);
        AbstractGui.fill(buttonX0 - 1, buttonY0 - 1, buttonX0, buttonY1 + 1, 0x88FFFFFF);
        AbstractGui.fill(buttonX0 - 1, buttonY1, buttonX1 + 1, buttonY1 + 1, 0x88000000);
        AbstractGui.fill(buttonX1, buttonY0 - 1, buttonX1 + 1, buttonY1 + 1, 0x88000000);
    }

    public boolean isInBox(double x, double y)
    {
        return x >= x0 && x <= x1 && y >= y0 && y <= y1;
    }

    public void onLeftClick()
    {
        Meson.debug("[CLIENT] clicked quest badge: " + quest.getId());
        Quests.client.showQuest(quest);
//        Optional<IQuest> qq = QuestClient.currentQuests.stream().filter(q -> q.getId().equals(quest.getId())).findFirst();
//        PacketHandler.sendToServer(new ServerQuestAction(ServerQuestAction.SHOW, quest.getId(), Hand.MAIN_HAND));
    }
}
