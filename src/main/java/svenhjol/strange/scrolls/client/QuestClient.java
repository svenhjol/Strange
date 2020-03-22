package svenhjol.strange.scrolls.client;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.meson.Meson;
import svenhjol.meson.helper.ClientHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.base.helper.VersionHelper;
import svenhjol.strange.scrolls.client.gui.QuestBadgeGui;
import svenhjol.strange.scrolls.client.screen.QuestScreen;
import svenhjol.strange.scrolls.client.screen.ScrollScreen;
import svenhjol.strange.scrolls.client.toast.QuestToast;
import svenhjol.strange.scrolls.client.toast.QuestToastTypes;
import svenhjol.strange.scrolls.message.ServerQuestList;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class QuestClient {
    public static List<IQuest> currentQuests = new ArrayList<>();

    public static long lastQuery;

    public void showQuest(IQuest quest) {
        PlayerEntity player = ClientHelper.getClientPlayer();
        Minecraft.getInstance().displayGuiScreen(new QuestScreen(player, quest));
    }

    public void showScroll(Hand hand) {
        PlayerEntity player = ClientHelper.getClientPlayer();
        Minecraft.getInstance().displayGuiScreen(new ScrollScreen(player, hand));
    }

    public void toast(IQuest quest, QuestToastTypes.Type type, String title) {
        Minecraft.getInstance().getToastGui().add(new QuestToast(quest, type, title, quest.getTitle()));
    }

    private final List<QuestBadgeGui> questBadges = new ArrayList<>();

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onBackgroundDrawn(GuiScreenEvent.BackgroundDrawnEvent event) {
        Minecraft mc = Minecraft.getInstance();
        int delayTicks = 101;

        if (isValidQuestBadgeScreen(mc)) {
            questBadges.clear();

            if (QuestClient.lastQuery + delayTicks < mc.world.getGameTime()) {
                Meson.getInstance(Strange.MOD_ID).getPacketHandler().sendToServer(new ServerQuestList());
                QuestClient.lastQuery = mc.world.getGameTime();
            }

            int w = QuestBadgeGui.WIDTH;
            int numQuests = QuestClient.currentQuests.size();
            if (numQuests == 0) return;

            final MainWindow mainWindow = VersionHelper.getMainWindow(mc);

            int xPos = (mainWindow.getScaledWidth() / 2) - (numQuests * w / 2);
            int yPos = (mainWindow.getScaledHeight() / 4) - 50;

            for (int i = 0; i < numQuests; i++) {
                questBadges.add(new QuestBadgeGui(QuestClient.currentQuests.get(i), xPos + (i * (w + 10)), yPos));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onMouseClicked(GuiScreenEvent.MouseClickedEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (isValidQuestBadgeScreen(mc)) {
            double x = event.getMouseX();
            double y = event.getMouseY();

            if (event.getButton() == 0) {
                for (QuestBadgeGui badge : questBadges) {
                    if (badge.isInBox(x, y))
                        badge.onLeftClick();
                }
            }
        }
    }

    private boolean isValidQuestBadgeScreen(Minecraft mc) {
        return mc.currentScreen instanceof InventoryScreen || mc.currentScreen instanceof CreativeScreen;
    }
}
