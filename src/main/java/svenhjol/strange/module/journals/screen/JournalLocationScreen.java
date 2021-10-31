package svenhjol.strange.module.journals.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import svenhjol.charm.helper.ClientHelper;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.data.JournalLocation;
import svenhjol.strange.module.knowledge.KnowledgeClient;
import svenhjol.strange.module.knowledge.KnowledgeHelper;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class JournalLocationScreen extends BaseJournalScreen {
    protected static final int MAX_NAME_LENGTH = 50;
    protected static final int MIN_PHOTO_DISTANCE = 10;
    protected String name;
    protected EditBox nameField;
    protected JournalLocation location;
    protected DynamicTexture photoTexture = null;
    protected ResourceLocation registeredPhotoTexture = null;
    protected int photoFailureRetries = 0;
    protected boolean hasInitializedUpdateButtons = false;
    protected boolean hasInitializedPhotoButtons = false;
    protected boolean hasRenderedUpdateButtons = false;
    protected boolean hasRenderedPhotoButtons = false;
    protected boolean hasPhoto = false;
    protected List<GuiHelper.ButtonDefinition> updateButtons = new ArrayList<>();

    public JournalLocationScreen(JournalLocation location) {
        super(new TextComponent(location.getName()));

        this.name = location.getName();
        this.location = location.copy();
    }

    @Override
    protected void init() {
        super.init();

        if (minecraft == null) {
            return;
        }

        // set up the input field for editing the entry name
        nameField = new EditBox(font, (width / 2) - 65, 40, 130, 12, new TextComponent("NameField"));
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
            bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> saveAndGoBack(),
                new TranslatableComponent("gui.strange.journal.go_back")));

            // if player has empty map, add button to make a map of this location
            if (playerHasEmptyMap()) {
                updateButtons.add(
                    new GuiHelper.ButtonDefinition(b -> makeMap(),
                        new TranslatableComponent("gui.strange.journal.make_map"))
                );
            }

            // if player is near the location, add button to take a photo
            if (playerIsNearLocation()) {
                updateButtons.add(
                    new GuiHelper.ButtonDefinition(b -> takePhoto(),
                        new TranslatableComponent("gui.strange.journal.take_photo"))
                );
            }

            // always add an icon button
            updateButtons.add(
                new GuiHelper.ButtonDefinition(b -> chooseIcon(),
                    new TranslatableComponent("gui.strange.journal.choose_icon"))
            );

            // always add a save button
            updateButtons.add(
                new GuiHelper.ButtonDefinition(b -> saveAndGoBack(),
                    new TranslatableComponent("gui.strange.journal.save"))
            );

            hasInitializedUpdateButtons = true;
        }

        if (!hasInitializedPhotoButtons) {
            hasInitializedPhotoButtons = true;
        }

        hasRenderedUpdateButtons = false;
        hasRenderedPhotoButtons = false;
        hasPhoto = hasPhoto();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        int buttonWidth = 105;
        int buttonHeight = 20;

        // render icon next to title
        renderTitleIcon(location.getIcon());

        // render photo and buttons
        if (hasPhoto) {
            renderPhoto(poseStack);
        }

        // render update buttons
        if (!hasRenderedUpdateButtons) {
            int left = hasPhoto ? midX + 5 : midX - 50;
            int top = 60;
            int yOffset = 22;

            GuiHelper.renderButtons(this, width, font, updateButtons, left, top, 0, yOffset, buttonWidth, buttonHeight);
            hasRenderedUpdateButtons = true;
        }

        // render coordinates and runes for this location
        ClientHelper.getPlayer().ifPresent(player -> {
            renderRunes(poseStack, player);
            renderCoordinates(poseStack, player);
        });

        nameField.render(poseStack, mouseX, mouseY, delta);
    }

    protected void renderCoordinates(PoseStack poseStack, Player player) {
        if (!player.isCreative()) {
            return;
        }

        BlockPos pos = location.getBlockPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        String max = String.valueOf(Math.max(z, Math.max(x, y)));

        int left = midX - 60 - (max.length() * 4);
        int top = 130;
        int yOffset = 14;

        font.draw(poseStack, "X: " + x, left, top + yOffset, KNOWN_COLOR);
        font.draw(poseStack, "Y: " + y, left, top + (yOffset * 2), KNOWN_COLOR);
        font.draw(poseStack, "Z: " + z, left, top + (yOffset * 3), KNOWN_COLOR);
    }

    protected void renderRunes(PoseStack poseStack, Player player) {
        boolean isCreative = player.isCreative();

        if (journal == null) {
            return;
        }

        if (!DimensionHelper.isDimension(player.level, location.getDimension())) {
            return;
        }

        String runes = location.getRunes();
        String knownRunes = KnowledgeHelper.convertRunesWithLearnedRunes(runes, journal.getLearnedRunes());

        int left = isCreative ? midX + 9 : midX - 48;
        int top = 150;
        int xOffset = 13;
        int yOffset = 15;

        KnowledgeClient.renderRunesString(minecraft, poseStack, knownRunes, left, top, xOffset, yOffset, 8, 4, KNOWN_COLOR, UNKNOWN_COLOR, false);

//        for (int y = 0; y < 4; y++) {
//            for (int x = 0; x < 8; x++) {
//                if (index < knownRunes.length()) {
//                    Component rune;
//                    int color;
//
//                    String s = String.valueOf(knownRunes.charAt(index));
//                    if (s.equals(KnowledgeHelper.UNKNOWN)) {
//                        rune = new TextComponent(KnowledgeHelper.UNKNOWN);
//                        color = UNKNOWN_COLOR;
//                    } else {
//                        rune = new TextComponent(s).withStyle(StrangeFonts.ILLAGER_GLYPHS_STYLE);
//                        color = KNOWN_COLOR;
//                    }
//
//                    font.draw(poseStack, rune, left + (x * xOffset), top + (y * yOffset), color);
//                }
//                index++;
//            }
//        }
    }

    protected void renderPhoto(PoseStack poseStack) {
        if (minecraft == null) {
            return;
        }

        if (photoTexture == null) {
            try {
                File photoFile = getPhoto();
                if (photoFile == null)
                    throw new Exception("Null problems with file");

                RandomAccessFile raf = new RandomAccessFile(photoFile, "r");
                if (raf != null)
                    raf.close();

                InputStream stream = new FileInputStream(photoFile);
                NativeImage photo = NativeImage.read(stream);
                photoTexture = new DynamicTexture(photo);
                registeredPhotoTexture = minecraft.getTextureManager().register("photo", photoTexture);
                stream.close();

                if (photoTexture == null || registeredPhotoTexture == null)
                    throw new Exception("Null problems with texture / registered texture");

            } catch (Exception e) {
                LogHelper.warn(this.getClass(), "Error loading photo: " + e);
                photoFailureRetries++;

                if (photoFailureRetries > 2) {
                    LogHelper.error(getClass(), "Failure loading photo, aborting retries");
                    hasPhoto = false;
                    registeredPhotoTexture = null;
                    photoTexture = null;
                }
            }
        }

        if (registeredPhotoTexture != null) {
            RenderSystem.setShaderTexture(0, registeredPhotoTexture);
            poseStack.pushPose();
            poseStack.scale(0.425F, 0.32F, 0.425F);
            blit(poseStack, ((int)((width / 2) / 0.425F) - 265), 187, 0, 0, 256, 200);
            poseStack.popPose();
        }
    }

    /**
     * Add an area of the page that allows photo to be clicked.
     */
    @Override
    public boolean mouseClicked(double x, double y, int button) {
        int left = 112;
        int top = 60;
        int height = 64;
        int width = 109;

        if (hasPhoto && registeredPhotoTexture != null) {
            if (x > (midX - left) && x < midX - left + width
                && y > top && y < top + height) {
                minecraft.setScreen(new JournalPhotoScreen(location));
                return true;
            }
        }

        return super.mouseClicked(x, y, button);
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
        if (minecraft != null && minecraft.player != null) {
            minecraft.setScreen(null);
            minecraft.options.hideGui = true;
            JournalsClient.locationBeingPhotographed = location;
            JournalsClient.photoTicks = 1;
        }
    }

    @Nullable
    protected File getPhoto() {
        if (minecraft == null) {
            return null;
        }

        File screenshotsDirectory = new File(minecraft.gameDirectory, "screenshots");
        return new File(screenshotsDirectory, location.getId() + ".png");
    }

    private boolean hasPhoto() {
        File photo = getPhoto();
        return photo != null && photo.exists();
    }

    protected boolean playerHasEmptyMap() {
        if (minecraft != null && minecraft.player != null) {
            return minecraft.player.getInventory().contains(new ItemStack(Items.MAP));
        }

        return false;
    }

    protected boolean playerIsNearLocation() {
        if (minecraft != null && minecraft.player != null) {
            return WorldHelper.getDistanceSquared(minecraft.player.blockPosition(), location.getBlockPos()) < MIN_PHOTO_DISTANCE;
        }

        return false;
    }

    private void nameFieldResponder(String text) {
        location.setName(text);
    }
}
