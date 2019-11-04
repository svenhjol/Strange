package svenhjol.strange.scrolls.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.handler.PacketHandler;
import svenhjol.strange.scrolls.message.ServerQuestList;
import svenhjol.strange.scrolls.client.gui.QuestBadgeGui;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class QuestClientEvents
{
    private List<QuestBadgeGui> questBadges = new ArrayList<>();

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onBackgroundDrawn(GuiScreenEvent.BackgroundDrawnEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        int delayTicks = 100;

        if (isValidQuestBadgeScreen(mc)) {
            if (QuestClient.lastQuery + delayTicks < mc.world.getGameTime()) {
                PacketHandler.sendToServer(new ServerQuestList());
                QuestClient.lastQuery = mc.world.getGameTime();
            }

            int w = QuestBadgeGui.WIDTH;
            int numQuests = QuestClient.currentQuests.size();
            if (numQuests == 0) return;

            int xPos = (mc.mainWindow.getScaledWidth() / 2) - (numQuests*w/2);
            int yPos = (mc.mainWindow.getScaledHeight() / 4) - 50;

            questBadges.clear();
            for (int i = 0; i < numQuests; i++) {
                questBadges.add(new QuestBadgeGui(QuestClient.currentQuests.get(i), xPos + (i * (w + 10)), yPos));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onMouseClicked(GuiScreenEvent.MouseClickedEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (isValidQuestBadgeScreen(mc)) {
            double x = event.getMouseX();
            double y = event.getMouseY();

            if (event.getButton() == 0) {
                for (QuestBadgeGui badge : questBadges) {
                    if (badge.isInBox(x, y)) badge.onLeftClick();
                }
            }
        }
    }

    private boolean isValidQuestBadgeScreen(Minecraft mc)
    {
        return mc.currentScreen instanceof InventoryScreen || mc.currentScreen instanceof CreativeScreen;
    }
}
