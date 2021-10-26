package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.JournalData;
import svenhjol.strange.module.journals.data.JournalLocation;

import java.util.List;

public class JournalLocationsScreen extends BaseJournalScreen {
    public static final int PER_PAGE = 6;
    public static final int NAME_CUTOFF = 27;

    protected boolean hasRenderedLocationButtons = false;
    protected int lastPage = 0;
    protected JournalData data;
    protected List<JournalLocation> locations;

    public JournalLocationsScreen() {
        super(new TranslatableComponent("gui.strange.journal.locations"));
        JournalsClient.getPlayerData().ifPresent(data -> this.data = data);

        // break out locations
        locations = data.getLocations();

        // "add location" button to the bottom
        bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> add(),
            new TranslatableComponent("gui.strange.journal.add_location")));
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();
        hasRenderedLocationButtons = false;
    }

    @Override
    public void renderTitle(PoseStack poseStack, int titleX, int titleY, int titleColor) {
        super.renderTitle(poseStack, titleX, 16, titleColor);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        int mid = width / 2;
        int buttonWidth = 140;
        int buttonHeight = 20;
        int paginationY = 180;
        int yOffset = 21;

        if (data == null || locations == null || locations.size() == 0) {
            // no locations, show "add location" button and exit early
            if (!hasRenderedLocationButtons) {
                addRenderableWidget(new Button(mid - (buttonWidth / 2), 28, buttonWidth, buttonHeight, new TranslatableComponent("gui.strange.journal.add_location"), b -> add()));
                hasRenderedLocationButtons = true;
            }
            return;
        }

        int numberOfLocations = locations.size();
        int y = titleY + 10; // start rendering below the title
        int currentPage = lastPage - 1;
        List<JournalLocation> sublist;

        if (numberOfLocations > PER_PAGE) {
            if (currentPage * PER_PAGE >= numberOfLocations || currentPage * PER_PAGE < 0) {
                // out of range, reset
                lastPage = 1;
                currentPage = 0;
            }
            sublist = locations.subList(currentPage * PER_PAGE, Math.min(currentPage * PER_PAGE + PER_PAGE, numberOfLocations));
        } else {
            sublist = locations;
        }

        for (JournalLocation location : sublist) {
            String name = getTruncatedName(location.getName());
            ItemStack icon = location.getIcon();

            // render item icons each time
            itemRenderer.renderGuiItem(icon, mid - (buttonWidth / 2) - 12, y + 2);

            // only render buttons on the first render pass
            if (!hasRenderedLocationButtons) {
                addRenderableWidget(new Button(mid - (buttonWidth / 2) + 6, y, buttonWidth, buttonHeight, new TextComponent(name), b -> select(location)));
            }

            y += yOffset;
        }

        if (numberOfLocations > PER_PAGE) {
            // render the page number each time
            centeredText(poseStack, font, new TranslatableComponent("gui.strange.journal.page", lastPage), mid, paginationY + 6, secondaryColor);

            // only render pagination buttons on the first render pass
            if (!hasRenderedLocationButtons) {
                if (lastPage * PER_PAGE < numberOfLocations) {
                    this.addRenderableWidget(new ImageButton(mid + 30, paginationY, 20, 18, 120, 0, 18, NAVIGATION, b -> {
                        ++lastPage;
                        init();
                    }));
                }
                if (lastPage > 1) {
                    this.addRenderableWidget(new ImageButton(mid - 50, paginationY, 20, 18, 140, 0, 18, NAVIGATION, b -> {
                        --lastPage;
                        init();
                    }));
                }
            }
        }

        hasRenderedLocationButtons = true;
    }

    protected void select(JournalLocation location) {
        ClientHelper.getClient().ifPresent(client -> client.setScreen(new JournalLocationScreen(location)));
    }

    protected void add() {
        JournalsClient.sendAddLocation();
    }

    protected String getTruncatedName(String name) {
        return name.substring(0, Math.min(name.length(), NAME_CUTOFF));
    }
}
