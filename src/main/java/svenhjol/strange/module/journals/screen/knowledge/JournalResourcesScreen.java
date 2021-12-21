package svenhjol.strange.module.journals.screen.knowledge;

import net.minecraft.network.chat.Component;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals.screen.JournalPaginatedScreen;

public abstract class JournalResourcesScreen<T> extends JournalPaginatedScreen<T> {
    public JournalResourcesScreen(Component component) {
        super(component);
    }

    @Override
    protected void init() {
        super.init();
        paginator.setButtonWidth(180);
    }

    /**
     * All knowledge resources screens go back to the main knowledge page.
     */
    @Override
    protected void addButtons() {
        bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> knowledge(), GO_BACK));
    }
}
