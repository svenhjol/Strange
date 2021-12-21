package svenhjol.strange.module.journals.screen.mini;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.paginator.StructurePaginator;
import svenhjol.strange.module.journals.screen.JournalScreen;
import svenhjol.strange.module.journals.screen.MiniJournal;
import svenhjol.strange.module.knowledge.Knowledge;

public class MiniStructuresScreen extends BaseMiniScreen {
    private StructurePaginator paginator;

    public MiniStructuresScreen(MiniJournal mini) {
        super(mini);
    }

    @Override
    public void init() {
        super.init();

        if (mini.selectedStructure != null) {

            mini.addBackButton(b -> {
                mini.selectedStructure = null;
                mini.changeSection(MiniJournal.Section.STRUCTURES);
            });

        } else {

            if (JournalsClient.journal == null) return;
            var structures = JournalsClient.journal.getLearnedStructures();
            if (structures == null) return;

            paginator = new StructurePaginator(structures);
            setPaginatorDefaults(paginator);
            paginator.setButtonWidth(94);

            paginator.init(screen, mini.offset, midX - 87, midY - 78, structure -> {
                mini.selectedStructure = structure;
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
        mini.renderTitle(poseStack, JournalScreen.LEARNED_STRUCTURES, midY - 94);

        if (mini.selectedStructure != null) {

            var knowledge = Knowledge.getKnowledge().orElse(null);
            if (knowledge == null) return;

            // Get the runes for the selected structure.
            var runes = knowledge.structureBranch.get(mini.selectedStructure);
            if (runes == null) return;

            runeStringRenderer.render(poseStack, font, runes);

        } else {

            paginator.render(poseStack, itemRenderer, font);

        }
    }
}
