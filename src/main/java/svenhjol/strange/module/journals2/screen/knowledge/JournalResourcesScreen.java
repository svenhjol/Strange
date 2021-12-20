package svenhjol.strange.module.journals2.screen.knowledge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals2.screen.JournalScreen;
import svenhjol.strange.module.journals2.paginator.ResourcePaginator;

public abstract class JournalResourcesScreen extends JournalScreen {
    protected ResourcePaginator paginator;

    public JournalResourcesScreen(Component component) {
        super(component);
    }

    @Override
    protected void init() {
        super.init();

        // Set up the resource paginator.
        paginator = getPaginator();

        paginator.init(this, offset, midX, 40, newOffset -> {
            offset = newOffset;
            init(minecraft, width, height);
        });

        bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> knowledge(), GO_BACK));

        setViewedPage();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        paginator.render(poseStack, itemRenderer, font);
    }

    protected abstract ResourcePaginator getPaginator();

    protected abstract void setViewedPage();
}
