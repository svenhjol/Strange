package svenhjol.strange.module.journals.screen.mini;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.helper.GuiHelper.ButtonDefinition;
import svenhjol.strange.module.journals.screen.JournalResources;
import svenhjol.strange.module.journals.screen.MiniJournal;
import svenhjol.strange.module.journals.screen.MiniJournal.Section;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MiniHomeScreen extends BaseMiniScreen {
    private List<ButtonDefinition> homeButtons = new ArrayList<>();

    public MiniHomeScreen(MiniJournal mini) {
        super(mini);
    }

    @Override
    public void init() {
        super.init();

        homeButtons = Arrays.asList(
            new ButtonDefinition(b -> mini.changeSection(Section.BOOKMARKS), JournalResources.BOOKMARKS),
            new ButtonDefinition(b -> mini.changeSection(Section.BIOMES), JournalResources.LEARNED_BIOMES),
            new ButtonDefinition(b -> mini.changeSection(Section.STRUCTURES), JournalResources.LEARNED_STRUCTURES),
            new ButtonDefinition(b -> mini.changeSection(Section.DIMENSIONS), JournalResources.LEARNED_DIMENSIONS)
        );
    }

    @Override
    public void firstRender(PoseStack poseStack, Font font, ItemRenderer itemRenderer) {
        int x = journalMidX - (mini.buttonWidth / 2);
        int y = midY - 60; // start rendering buttons from here
        int xOffset = 0;
        int yOffset = 24;

        GuiHelper.addButtons(screen, screen.width, screen.font, homeButtons, x, y, xOffset, yOffset, buttonWidth, buttonHeight);
    }

    @Override
    public void render(PoseStack poseStack, ItemRenderer itemRenderer, Font font) {
        mini.renderTitle(poseStack, JournalResources.JOURNAL, midY - 86);
    }
}
