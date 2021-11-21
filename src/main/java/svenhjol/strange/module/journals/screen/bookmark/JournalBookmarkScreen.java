package svenhjol.strange.module.journals.screen.bookmark;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import svenhjol.charm.helper.*;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals.JournalBookmark;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.journals.JournalsClient;
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
    protected Component dimensionName;
    protected EditBox nameField;
    protected JournalBookmark bookmark;
    protected DynamicTexture photoTexture = null;
    protected ResourceLocation registeredPhotoTexture = null;
    protected int photoFailureRetries;
    protected int maxNameLength;
    protected int minPhotoDistance;
    protected boolean hasInitializedUpdateButtons = false;
    protected boolean hasRenderedUpdateButtons = false;
    protected boolean hasPhoto = false;
    protected List<GuiHelper.ButtonDefinition> updateButtons = new ArrayList<>();

    public JournalBookmarkScreen(JournalBookmark bookmark) {
        super(new TextComponent(bookmark.getName()));

        this.name = bookmark.getName();
        this.bookmark = bookmark.copy();
        this.photoFailureRetries = 0;
        this.maxNameLength = 50;
        this.minPhotoDistance = 10;
        this.dimensionName = new TranslatableComponent("gui.strange.journal.dimension", StringHelper.snakeToPretty(this.bookmark.getDimension().getPath(), true));

        this.bottomNavButtons.add(
            new GuiHelper.ImageButtonDefinition(b -> delete(), NAVIGATION, 20, 0, 18, DELETE_TOOLTIP)
        );

        this.bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> saveAndGoBack(), GO_BACK));
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

        ClientHelper.getPlayer().ifPresent(player -> {
            // add map icon if player has an empty map
            if (playerCanMakeMap()) {
                this.rightNavButtons.add(
                    new GuiHelper.ImageButtonDefinition(b -> makeMap(), NAVIGATION, 40, 0, 18, MAKE_MAP_TOOLTIP)
                );
            }

            // add take picture icon if player is near the location
            if (playerIsNearBookmark()) {
                this.rightNavButtons.add(
                    new GuiHelper.ImageButtonDefinition(b -> takePhoto(), NAVIGATION, 80, 0, 18, TAKE_PHOTO_TOOLTIP)
                );
            }
        });

        if (!hasInitializedUpdateButtons) {
            if (playerIsNearBookmark()) {
                updateButtons.add(new GuiHelper.ButtonDefinition(b -> takePhoto(), TAKE_PHOTO));
            }
            updateButtons.add(new GuiHelper.ButtonDefinition(b -> chooseIcon(), CHOOSE_ICON));
            updateButtons.add(new GuiHelper.ButtonDefinition(b -> saveAndGoBack(), SAVE));
            hasInitializedUpdateButtons = true;
        }

        hasRenderedUpdateButtons = false;
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

        // render dimension name for this bookmark
        renderDimensionName(poseStack);

        // render coordinates and runes for this bookmark
        ClientHelper.getPlayer().ifPresent(player -> renderRunes(poseStack, player));

        nameField.render(poseStack, mouseX, mouseY, delta);
    }

    protected void renderDimensionName(PoseStack poseStack) {
        int top = 137;
        GuiHelper.drawCenteredString(poseStack, font, dimensionName, midX, top, secondaryColor);
    }

    protected void renderRunes(PoseStack poseStack, Player player) {
        // in creative mode just show the coordinates not the runes
        if (player.isCreative()) {
            BlockPos pos = bookmark.getBlockPos();
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            int top = 158;

            GuiHelper.drawCenteredString(poseStack, font, new TextComponent(x + " " + y + " " + z), midX, top, knownColor);
            return;
        }

        if (journal == null) return;
        if (!DimensionHelper.isDimension(player.level, bookmark.getDimension())) return;

        String runes = bookmark.getRunes();
        String knownRunes = KnowledgeHelper.convertRunesWithLearnedRunes(runes, journal.getLearnedRunes());

        int left = midX - 61;
        int top = 158;
        int xOffset = 13;
        int yOffset = 15;

        KnowledgeClient.renderRunesString(minecraft, poseStack, knownRunes, left, top, xOffset, yOffset, 10, 4, knownColor, unknownColor, false);
    }

    protected void renderPhoto(PoseStack poseStack) {
        if (minecraft == null) return;

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
        if (minecraft != null) {
            minecraft.setScreen(new JournalChooseIconScreen(bookmark));
        }
    }

    protected void makeMap() {
        JournalsClient.sendMakeMap(bookmark);
        if (minecraft != null) {
            minecraft.setScreen(null);
        }
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

    protected boolean playerCanMakeMap() {
        if (minecraft != null && minecraft.player != null) {
            if (DimensionHelper.isDimension(minecraft.player.level, bookmark.getDimension())) {
                return minecraft.player.getInventory().contains(new ItemStack(Items.MAP));
            }
        }

        return false;
    }

    protected boolean playerIsNearBookmark() {
        if (minecraft != null && minecraft.player != null) {
            if (DimensionHelper.isDimension(minecraft.player.level, bookmark.getDimension())) {
                return WorldHelper.getDistanceSquared(minecraft.player.blockPosition(), bookmark.getBlockPos()) < minPhotoDistance;
            }
        }

        return false;
    }

    private void nameFieldResponder(String text) {
        bookmark.setName(text);
    }
}
