package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.mixin.screen.ScreenAccessor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.data.JournalLocation;

import java.util.ArrayList;
import java.util.List;

public class JournalLocationScreen extends BaseJournalScreen {
    protected static final int MAX_NAME_LENGTH = 50;
    protected static final int MIN_PHOTO_DISTANCE = 10;
    protected String name;
    protected EditBox nameField;
    protected JournalLocation location;

    protected boolean hasInitializedUpdateButtons = false;
    protected boolean hasRenderedUpdateButtons = false;
    protected List<ButtonDefinition> updateButtons = new ArrayList<>();

    public JournalLocationScreen(JournalLocation location) {
        super(new TextComponent(location.getName()));

        this.name = location.getName();
        this.location = location.copy();
    }

    @Override
    protected void init() {
        super.init();

        if (minecraft == null)
            return;

        // set up the input field for editing the entry name
        nameField = new EditBox(font, (width / 2) + 5, 40, 105, 12, new TextComponent("NameField"));
        nameField.changeFocus(true);
        nameField.setCanLoseFocus(false);
        nameField.setTextColor(-1);
        nameField.setTextColorUneditable(-1);
        nameField.setBordered(true);
        nameField.setMaxLength(MAX_NAME_LENGTH);
        nameField.setResponder(this::nameFieldResponder);
        nameField.setValue(name);
        nameField.setEditable(true);

        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.children.add(nameField);
        setFocused(nameField);

        if (!hasInitializedUpdateButtons) {
            // add a back button at the bottom
            bottomButtons.add(0, new ButtonDefinition(b -> saveAndGoBack(),
                new TranslatableComponent("gui.strange.journal.go_back")));

            // if player has empty map, add button to make a map of this location
            if (playerHasEmptyMap()) {
                updateButtons.add(
                    new ButtonDefinition(b -> makeMap(),
                        new TranslatableComponent("gui.strange.journal.make_map"))
                );
            }

            // if player is near the location, add button to take a photo
            if (playerIsNearLocation()) {
                updateButtons.add(
                    new ButtonDefinition(b -> takePhoto(),
                        new TranslatableComponent("gui.strange.journal.take_photo"))
                );
            }

            // always add an icon button
            updateButtons.add(
                new ButtonDefinition(b -> chooseIcon(),
                    new TranslatableComponent("gui.strange.journal.choose_icon"))
            );

            // always add a save button
            updateButtons.add(
                new ButtonDefinition(b -> saveAndGoBack(),
                    new TranslatableComponent("gui.strange.journal.save"))
            );

            hasInitializedUpdateButtons = true;
        }

        hasRenderedUpdateButtons = false;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        int mid = width / 2;

        // render icon next to title
        ItemStack icon = location.getIcon();
        int iconX = width / 2 - 17 - ((this.title.getString().length() * 6) / 2);
        itemRenderer.renderGuiItem(icon, iconX, titleY - 5);


        // render left-side page


        if (!hasRenderedUpdateButtons) {
            int buttonWidth = 105;
            int buttonHeight = 20;
            int left = mid + 5;
            int yStart = 150;
            int yOffset = -22;

            renderButtons(updateButtons, left, yStart, 0, yOffset, buttonWidth, buttonHeight);
            hasRenderedUpdateButtons = true;
        }

        nameField.render(poseStack, mouseX, mouseY, delta);
    }

    /**
     * We need to resync the journal when leaving this page to go back to locations.
     */
    protected void saveAndGoBack() {
        save(); // save progress before changing screen
        JournalsClient.sendOpenJournal(Journals.Page.LOCATIONS);
    }

    protected void save() {
        JournalsClient.sendUpdateLocation(location);
    }

    protected void chooseIcon() {
        save(); // save progress before changing screen
        if (minecraft != null)
            minecraft.setScreen(new JournalChooseIconScreen(location));
    }

    protected void makeMap() {

    }

    protected void takePhoto() {

    }

    protected boolean playerHasEmptyMap() {
        if (minecraft != null && minecraft.player != null)
            return minecraft.player.getInventory().contains(new ItemStack(Items.MAP));

        return false;
    }

    protected boolean playerIsNearLocation() {
        if (minecraft != null && minecraft.player != null)
            return WorldHelper.getDistanceSquared(minecraft.player.blockPosition(), location.getBlockPos()) < MIN_PHOTO_DISTANCE;

        return false;
    }

    private void nameFieldResponder(String text) {
        location.setName(text);
    }
}
