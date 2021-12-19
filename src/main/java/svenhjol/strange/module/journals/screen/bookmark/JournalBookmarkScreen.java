package svenhjol.strange.module.journals.screen.bookmark;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.StringHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.helper.GuiHelper.ButtonDefinition;
import svenhjol.strange.module.bookmarks.Bookmark;
import svenhjol.strange.module.bookmarks.BookmarksClient;
import svenhjol.strange.module.journals.Journals;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.screen.JournalScreen;
import svenhjol.strange.module.journals2.photo.BookmarkPhoto;
import svenhjol.strange.module.journals2.Journals2Client;
import svenhjol.strange.module.journals2.helper.Journal2Helper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class JournalBookmarkScreen extends JournalScreen {
    protected String name;
    protected Component dimensionName;
    protected EditBox nameEditBox;
    protected Bookmark bookmark;
    protected int maxNameLength;
    protected int minPhotoDistance;
    protected int buttonWidth;
    protected int buttonHeight;
    protected List<ButtonDefinition> pageButtons = new ArrayList<>();
    protected BookmarkPhoto photo;

    public JournalBookmarkScreen(@Nonnull Bookmark bookmark) {
        super(new TextComponent(bookmark.getName()));

        this.name = bookmark.getName();
        this.bookmark = bookmark.copy();
        this.maxNameLength = 50;
        this.minPhotoDistance = 10;
        this.dimensionName = new TranslatableComponent("gui.strange.journal.dimension", StringHelper.snakeToPretty(this.bookmark.getDimension().getPath(), true));

        this.buttonWidth = 105;
        this.buttonHeight = 20;

        Journals2Client.tracker.setBookmark(Journals.Page.BOOKMARK, bookmark);
    }

    @Override
    protected void init() {
        super.init();

        initEditBox();

        photo = new BookmarkPhoto(minecraft, bookmark);

        pageButtons = new ArrayList<>();

        if (playerIsNearBookmark()) {
            pageButtons.add(new ButtonDefinition(b -> takePhoto(), TAKE_PHOTO));
        }

        pageButtons.add(new ButtonDefinition(b -> chooseIcon(), CHOOSE_ICON));
        pageButtons.add(new ButtonDefinition(b -> saveAndGoBack(), SAVE));

        bottomNavButtons.add(new GuiHelper.ImageButtonDefinition(b -> remove(), NAVIGATION, 20, 0, 18, DELETE_TOOLTIP));
        bottomButtons.add(0, new ButtonDefinition(b -> saveAndGoBack(), GO_BACK));

        // add map icon if player has an empty map
        if (playerCanMakeMap()) {
            rightNavButtons.add(new GuiHelper.ImageButtonDefinition(b -> makeMap(), NAVIGATION, 40, 0, 18, MAKE_MAP_TOOLTIP));
        }

        // add take picture icon if player is near the location
        if (playerIsNearBookmark()) {
            rightNavButtons.add(new GuiHelper.ImageButtonDefinition(b -> takePhoto(), NAVIGATION, 80, 0, 18, TAKE_PHOTO_TOOLTIP));
        }
    }

    @Override
    protected void firstRender(PoseStack poseStack) {
        super.firstRender(poseStack);

        int left = photo.isValid() ? midX + 5 : midX - 50;
        int top = 60;
        int yOffset = 22;

        GuiHelper.addButtons(this, width, font, pageButtons, left, top, 0, yOffset, buttonWidth, buttonHeight);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);

        renderTitleIcon(getBookmarkIconItem(bookmark));
        renderPhoto(poseStack);
        renderDimensionName(poseStack);
        renderRunes(poseStack);

        nameEditBox.render(poseStack, mouseX, mouseY, delta);
    }

    protected void renderDimensionName(PoseStack poseStack) {
        int top = 137;
        GuiHelper.drawCenteredString(poseStack, font, dimensionName, midX, top, secondaryColor);
    }

    protected void renderRunes(PoseStack poseStack) {
        // When in creative mode just show the XYZ coordinates rather than the runes.
        if (minecraft.player.isCreative()) {
            BlockPos pos = bookmark.getBlockPos();
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            int top = 158;

            GuiHelper.drawCenteredString(poseStack, font, new TextComponent(x + " " + y + " " + z), midX, top, knownColor);
            return;
        }

        // Don't show anything if the player hasn't learned any runes.
        if (Journal2Helper.getLearnedRunes().isEmpty()) return;

        // The player must be in the same dimension as the bookmark.
        if (!DimensionHelper.isDimension(minecraft.player.level, bookmark.getDimension())) return;

        int left = midX - 61;
        int top = 158;
        int xOffset = 13;
        int yOffset = 15;

        renderRunesString(poseStack, bookmark.getRunes(), left, top, xOffset, yOffset, 10, 4, false);
    }

    protected void renderPhoto(PoseStack poseStack) {
        if (photo.isValid()) {
            RenderSystem.setShaderTexture(0, photo.getTexture());
            poseStack.pushPose();
            poseStack.scale(0.425F, 0.32F, 0.425F);
            blit(poseStack, ((int)(midX / 0.425F) - 265), 187, 0, 0, 256, 200);
            poseStack.popPose();
        }
    }

    /**
     * Handle mouse clicked on this screen.
     * Add an area of the page that allows the photo to be clicked.
     */
    @Override
    public boolean mouseClicked(double x, double y, int button) {
        int left = 112;
        int top = 60;
        int height = 64;
        int width = 109;

        // This adds a trigger area around the photo that allows the player to click and show a bigger version.
        if (photo.isValid()) {
            if (x > (midX - left) && x < midX - left + width && y > top && y < top + height) {
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

    @Override
    public void onClose() {
        Journals2Client.tracker.setPage(Journals.Page.BOOKMARKS);
        super.onClose();
    }

    protected void save() {
        BookmarksClient.sendUpdateBookmark(bookmark);
    }

    protected void remove() {
        BookmarksClient.sendRemoveBookmark(bookmark);
    }

    protected void chooseIcon() {
        minecraft.setScreen(new JournalChooseIconScreen(bookmark));
    }

    protected void makeMap() {
        save();
        Journals2Client.sendMakeMap(bookmark);
        minecraft.setScreen(null);
    }

    protected void takePhoto() {
        Journals2Client.photo.setBookmark(bookmark);
    }

    protected boolean playerCanMakeMap() {
        return DimensionHelper.isDimension(minecraft.player.level, bookmark.getDimension())
            && minecraft.player.getInventory().contains(new ItemStack(Items.MAP));
    }

    protected boolean playerIsNearBookmark() {
        return DimensionHelper.isDimension(minecraft.player.level, bookmark.getDimension())
            && WorldHelper.getDistanceSquared(minecraft.player.blockPosition(), bookmark.getBlockPos()) < minPhotoDistance;
    }

    private void initEditBox() {
        nameEditBox = new EditBox(font, midX - 65, 40, 130, 12, new TextComponent("NameField"));
        nameEditBox.changeFocus(true);
        nameEditBox.setCanLoseFocus(false);
        nameEditBox.setTextColor(-1);
        nameEditBox.setTextColorUneditable(-1);
        nameEditBox.setBordered(true);
        nameEditBox.setMaxLength(maxNameLength);
        nameEditBox.setResponder(text -> bookmark.setName(text));
        nameEditBox.setValue(name);
        nameEditBox.setEditable(true);

        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        children.add(nameEditBox);
        setFocused(nameEditBox);
    }
}
