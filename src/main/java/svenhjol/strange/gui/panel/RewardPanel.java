package svenhjol.strange.gui.panel;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.scroll.Reward;
import svenhjol.strange.scroll.ScrollQuest;

public class RewardPanel extends GuiHelper {
    public static void render(Screen screen, MatrixStack matrices, ScrollQuest quest, int mid, int width, int ypos, int mouseX, int mouseY) {
        Reward reward = quest.getReward();
        drawCenteredTitle(matrices, I18n.translate("gui.strange.scrolls.reward"), mid, ypos);
    }
}
