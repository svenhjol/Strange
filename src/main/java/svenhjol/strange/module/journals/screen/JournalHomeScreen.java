package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.helper.GuiHelper.ButtonDefinition;
import svenhjol.strange.module.journals2.Journals2Client;
import svenhjol.strange.module.journals2.PageTracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class JournalHomeScreen extends JournalScreen {
    protected List<ButtonDefinition> homeButtons = new ArrayList<>();

    public JournalHomeScreen() {
        super(JOURNAL);
        Journals2Client.tracker.setPage(PageTracker.Page.HOME);
    }

    @Override
    protected void init() {
        super.init();

        homeButtons = new ArrayList<>(Arrays.asList(
            new ButtonDefinition(b -> bookmarks(), BOOKMARKS),
            new ButtonDefinition(b -> quests(), QUESTS),
            new ButtonDefinition(b -> knowledge(), KNOWLEDGE)
        ));
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return COVER_BACKGROUND;
    }

    @Override
    public void renderTitle(PoseStack poseStack, int titleX, int titleY, int titleColor) {
        // The homescreen requires the title's height to be customized.
        super.renderTitle(poseStack, titleX, 25, titleColor);
    }

    @Override
    public void renderNavigation() {
        // Don't add the navigation buttons on the homescreen.
    }

    @Override
    protected void firstRender(PoseStack poseStack) {
        super.firstRender(poseStack);

        int buttonWidth = 100;
        int buttonHeight = 20;
        int x = midX - (buttonWidth / 2);
        int y = 48;
        int xOffset = 0;
        int yOffset = 24;

        GuiHelper.addButtons(this, width, font, homeButtons, x, y, xOffset, yOffset, buttonWidth, buttonHeight);
    }
}
