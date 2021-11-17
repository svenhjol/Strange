package svenhjol.strange.module.journals.screen.bookmark;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
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
import svenhjol.strange.module.journals.JournalBookmark;
import svenhjol.strange.module.journals.screen.JournalScreen;
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
public class JournalBookmarkScreen extends JournalScreen {
    protected String name;
    protected EditBox nameField;
    protected JournalBookmark bookmark;
    protected DynamicTexture photoTexture = null;
    protected ResourceLocation registeredPhotoTexture = null;
    protected int photoFailureRetries;
    protected int maxNameLength;
    protected int minPhotoDistance;
    protected boolean hasInitializedUpdateButtons = false;
    protected boolean hasInitializedPhotoButtons = false;
    protected boolean hasRenderedUpdateButtons = false;
    protected boolean hasRenderedPhotoButtons = false;
    protected boolean hasPhoto = false;
    protected List<GuiHelper.ButtonDefinition> updateButtons = new ArrayList<>();

    public JournalBookmarkScreen(JournalBookmark bookmark) {
        super(new TextComponent(bookmark.getName()));

        this.name = bookmark.getName();
        this.bookmark = bookmark.copy();
        this.photoFailureRetries = 0;
        this.maxNameLength = 50;
        this.minPhotoDistance = 10;

        this.navigationButtons.add(
            new GuiHelper.ImageButtonDefinition(b -> delete(), NAVIGATION, 20, 0, 18, DELETE_TOOLTIP)
        );
    }

    @Override
    protected void init() {
        super.init();

        if (minecraft == null) {
            return;
        }

        // set up the input field for editing the entry name
        nameField = new EditBox(font, (midX) - 65, 40, 130, 12, new TextComponent("NameField"));
        nameField.changeFocus(true);
        nameField.setCanLoseFocus(false);
        nameField.setTextColor(-1);
        nameField.setTextColorUneditable(-1);
        nameField.setBordered(true);
        nameField.setMaxLength(maxNameLength);
        nameField.setResponder(this::nameFieldResponder);
        nameField.setValue(name);
        nameField.setEditable(true);

        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.children.add(nameField);
        setFocused(nameField);

        if (!hasInitializedUpdateButtons) {
            // add a back button at the bottom
            bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> saveAndGoBack(), GO_BACK));

            // if player has empty map, add button to make a map of this bookmark
            if (playerHasEmptyMap()) {
                updateButtons.add(new GuiHelper.ButtonDefinition(b -> makeMap(), MAKE_MAP));
            }

            // if player is near the bookmark, add button to take a photo
            if (playerIsNearBookmark()) {
                updateButtons.add(new GuiHelper.ButtonDefinition(b -> takePhoto(), TAKE_PHOTO));
            }

            // always add an icon button
            updateButtons.add(new GuiHelper.ButtonDefinition(b -> chooseIcon(), CHOOSE_ICON));

            // always add a save button
            updateButtons.add(new GuiHelper.ButtonDefinition(b -> saveAndGoBack(), SAVE));

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
        renderTitleIcon(bookmark.getIcon());

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

        // render coordinates and runes for this bookmark
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

        BlockPos pos = bookmark.getBlockPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        String max = String.valueOf(Math.max(z, Math.max(x, y)));

        int left = midX - 60 - (max.length() * 4);
        int top = 130;
        int yOffset = 14;

        font.draw(poseStack, "X: " + x, left, top + yOffset, knownColor);
        font.draw(poseStack, "Y: " + y, left, top + (yOffset * 2), knownColor);
        font.draw(poseStack, "Z: " + z, left, top + (yOffset * 3), knownColor);
    }

    protected void renderRunes(PoseStack poseStack, Player player) {
        boolean isCreative = player.isCreative();

        if (journal == null) {
            return;
        }

        if (!DimensionHelper.isDimension(player.level, bookmark.getDimension())) {
            return;
        }

        String runes = bookmark.getRunes();
        String knownRunes = KnowledgeHelper.convertRunesWithLearnedRunes(runes, journal.getLearnedRunes());

        int left = isCreative ? midX + 9 : midX - 48;
        int top = 150;
        int xOffset = 13;
        int yOffset = 15;

        KnowledgeClient.renderRunesString(minecraft, poseStack, knownRunes, left, top, xOffset, yOffset, 8, 4, knownColor, unknownColor, false);
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
            blit(poseStack, ((int)((midX) / 0.425F) - 265), 187, 0, 0, 256, 200);
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
                minecraft.setScreen(new JournalPhotoScreen(bookmark));
                return true;
            }
        }

        return super.mouseClicked(x, y, button);
    }

    /**
     * We need to resync the journal when leaving this page to go back to bookmarks.
     */
    protected void saveAndGoBack() {
        save(); // save progress before changing screen
        JournalsClient.sendOpenJournal(Journals.Page.BOOKMARKS);
    }

    protected void save() {
        JournalsClient.sendUpdateBookmark(bookmark);
    }

    protected void delete() {
        JournalsClient.sendDeleteBookmark(bookmark);
        JournalsClient.sendOpenJournal(Journals.Page.BOOKMARKS);
    }

    protected void chooseIcon() {
        save(); // save progress before changing screen
        if (minecraft != null)
            minecraft.setScreen(new JournalChooseIconScreen(bookmark));
    }

    protected void makeMap() {

    }

    protected void takePhoto() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.setScreen(null);
            minecraft.options.hideGui = true;
            JournalsClient.bookmarkBeingPhotographed = bookmark;
            JournalsClient.photoTicks = 1;
        }
    }

    @Nullable
    protected File getPhoto() {
        if (minecraft == null) {
            return null;
        }

        File screenshotsDirectory = new File(minecraft.gameDirectory, "screenshots");
        return new File(screenshotsDirectory, bookmark.getId() + ".png");
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

    protected boolean playerIsNearBookmark() {
        if (minecraft != null && minecraft.player != null) {
            return WorldHelper.getDistanceSquared(minecraft.player.blockPosition(), bookmark.getBlockPos()) < minPhotoDistance;
        }

        return false;
    }

    private void nameFieldResponder(String text) {
        bookmark.setName(text);
    }
}
