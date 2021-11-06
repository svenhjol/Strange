package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.module.journals.JournalData;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class JournalResourcesScreen extends JournalScreen {
    protected boolean hasRenderedButtons;

    public JournalResourcesScreen(Component component) {
        super(component);

        this.hasRenderedButtons = false;
    }

    @Override
    public void renderTitle(PoseStack poseStack, int titleX, int titleY, int titleColor) {
        super.renderTitle(poseStack, titleX, 16, titleColor);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        int buttonWidth = 180;
        int buttonHeight = 20;
        int yOffset = 21;

        if (journal == null) {
            return;
        }

        AtomicInteger y = new AtomicInteger(40);
        Consumer<ResourceLocation> renderItem = resource -> {
            String prettyName = StringHelper.snakeToPretty(resource.getPath(), true);
            String truncated = getTruncatedName(prettyName, 27);

            if (!hasRenderedButtons) {
                Button button = new Button(midX - (buttonWidth / 2), y.get(), buttonWidth, buttonHeight, new TextComponent(truncated), b -> select(resource));
                addRenderableWidget(button);
            }

            y.addAndGet(yOffset);
        };

        paginator(poseStack, getResources(journal), renderItem, getLabelForNoItem(), !hasRenderedButtons);
        hasRenderedButtons = true;
    }

    @Override
    protected void redraw() {
        super.redraw();
        hasRenderedButtons = false;
    }

    protected abstract List<ResourceLocation> getResources(JournalData journal);

    protected abstract Supplier<Component> getLabelForNoItem();

    protected abstract void select(ResourceLocation resource);
}
