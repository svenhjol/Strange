package svenhjol.strange.module.journals.screen.mini;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.paginator.DimensionPaginator;
import svenhjol.strange.module.journals.screen.JournalScreen;
import svenhjol.strange.module.journals.screen.MiniJournal;
import svenhjol.strange.module.knowledge.Knowledge;

public class MiniDimensionsScreen extends BaseMiniScreen {
    private DimensionPaginator paginator;

    public MiniDimensionsScreen(MiniJournal mini) {
        super(mini);
    }

    @Override
    public void init() {
        super.init();

        if (mini.selectedDimension != null) {

            mini.addBackButton(b -> {
                mini.selectedDimension = null;
                mini.changeSection(MiniJournal.Section.DIMENSIONS);
            });

        } else {

            if (JournalsClient.journal == null) return;
            var dimensions = JournalsClient.journal.getLearnedDimensions();
            if (dimensions == null) return;

            paginator = new DimensionPaginator(dimensions);
            setPaginatorDefaults(paginator);
            paginator.setButtonWidth(94);

            paginator.init(screen, mini.offset, midX - 87, midY - 78, dimension -> {
                mini.selectedDimension = dimension;
                mini.redraw();
            }, newOffset -> {
                mini.offset = newOffset;
                mini.redraw();
            });

            mini.addBackButton(b -> mini.changeSection(MiniJournal.Section.HOME));

        }
    }

    @Override
    public void render(PoseStack poseStack, ItemRenderer itemRenderer, Font font) {
        mini.renderTitle(poseStack, JournalScreen.LEARNED_DIMENSIONS, midY - 94);

        if (mini.selectedDimension != null) {

            var knowledge = Knowledge.getKnowledge().orElse(null);
            if (knowledge == null) return;

            // Get the runes for the selected dimension.
            var runes = knowledge.dimensionBranch.get(mini.selectedDimension);
            if (runes == null) return;

            runeStringRenderer.render(poseStack, font, runes);

        } else {

            paginator.render(poseStack, itemRenderer, font);

        }
    }
}
