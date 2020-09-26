package svenhjol.strange.scroll.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.scroll.tag.QuestTag;
import svenhjol.strange.scroll.tag.RewardTag;

public class RewardPanel extends GuiHelper {
    public static void render(Screen screen, MatrixStack matrices, QuestTag quest, int mid, int width, int ypos, int mouseX, int mouseY) {
        RewardTag rewardTag = quest.getReward();
        drawCenteredTitle(matrices, I18n.translate("gui.strange.scrolls.reward"), mid, ypos);
    }
}
