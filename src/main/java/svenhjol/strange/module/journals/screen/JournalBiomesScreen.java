package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.helper.StringHelper;
import svenhjol.strange.helper.GuiHelper;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class JournalBiomesScreen extends BaseJournalScreen {
    protected boolean hasRenderedButtons;

    public JournalBiomesScreen() {
        super(new TranslatableComponent("gui.strange.journal.learned_biomes"));

        // add a back button at the bottom
        this.bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> knowledge(),
            new TranslatableComponent("gui.strange.journal.go_back")));

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
        Supplier<Component> labelForNoItem =
            () -> new TranslatableComponent("gui.strange.journal.no_learned_biomes");

        Consumer<ResourceLocation> renderItem = biome -> {
            String prettyName = StringHelper.snakeToPretty(biome.getPath(), true);
            String truncated = getTruncatedName(prettyName, 27);

            if (!hasRenderedButtons) {
                Button button = new Button(midX - (buttonWidth / 2), y.get(), buttonWidth, buttonHeight, new TextComponent(truncated), b -> select(biome));
                addRenderableWidget(button);
            }

            y.addAndGet(yOffset);
        };

        paginator(poseStack, journal.getLearnedBiomes(), renderItem, labelForNoItem, !hasRenderedButtons);
        hasRenderedButtons = true;
    }

    @Override
    protected void redraw() {
        super.redraw();
        hasRenderedButtons = false;
    }

    protected void select(ResourceLocation biome) {
        ClientHelper.getClient().ifPresent(client
            -> client.setScreen(new JournalBiomeScreen(biome)));
    }
}
