package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.data.JournalLocation;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class JournalLocationsScreen extends JournalScreen {
    protected boolean hasRenderedButtons;

    public JournalLocationsScreen() {
        super(LOCATIONS);

        // "add location" button to the bottom
        this.bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> add(), ADD_LOCATION));

        this.hasRenderedButtons = false;
    }

    @Override
    public void renderTitle(PoseStack poseStack, int titleX, int titleY, int titleColor) {
        super.renderTitle(poseStack, titleX, 16, titleColor);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        int buttonWidth = 140;
        int buttonHeight = 20;
        int yOffset = 21;

        if (journal == null || journal.getLocations() == null || journal.getLocations().size() == 0) {
            // no locations, show "add location" button and exit early
            if (!hasRenderedButtons) {
                addRenderableWidget(new Button(midX - (buttonWidth / 2), 28, buttonWidth, buttonHeight, ADD_LOCATION, b -> add()));
                hasRenderedButtons = true;
            }
            return;
        }

        AtomicInteger y = new AtomicInteger(40);
        Supplier<Component> labelForNoItem = () -> NO_LOCATIONS;
        Consumer<JournalLocation> renderItem = location -> {
            String name = getTruncatedName(location.getName(), 27);
            ItemStack icon = location.getIcon();

            // render item icons each time
            itemRenderer.renderGuiItem(icon, midX - (buttonWidth / 2) - 12, y.get() + 2);

            // only render buttons on the first render pass
            if (!hasRenderedButtons) {
                Button button = new Button(midX - (buttonWidth / 2) + 6, y.get(), buttonWidth, buttonHeight, new TextComponent(name), b -> select(location));
                addRenderableWidget(button);
            }

            y.addAndGet(yOffset);
        };

        paginator(poseStack, journal.getLocations(), renderItem, labelForNoItem, !hasRenderedButtons);
        hasRenderedButtons = true;
    }

    protected void select(JournalLocation location) {
        ClientHelper.getClient().ifPresent(client -> client.setScreen(new JournalLocationScreen(location)));
    }

    protected void add() {
        JournalsClient.sendAddLocation();
    }
}
