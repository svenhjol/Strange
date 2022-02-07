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
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.PageTracker;
import svenhjol.strange.module.journals.helper.JournalHelper;
import svenhjol.strange.module.journals.photo.BookmarkPhoto;
import svenhjol.strange.module.journals.screen.JournalResources;
import svenhjol.strange.module.journals.screen.JournalScreen;
import svenhjol.strange.module.runes.client.RuneStringRenderer;

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
    protected RuneStringRenderer runeStringRenderer;

    public JournalBookmarkScreen(@Nonnull Bookmark bookmark) {
        super(new TextComponent(bookmark.getName().substring(0, Math.min(bookmark.getName().length(), 36))));

        this.name = bookmark.getName();
        this.bookmark = bookmark.copy();
        this.maxNameLength = 60;
        this.minPhotoDistance = 10;
        this.dimensionName = new TranslatableComponent("gui.strange.journal.dimension", StringHelper.snakeToPretty(this.bookmark.getDimension().getPath(), true));

        this.buttonWidth = 105;
        this.buttonHeight = 20;

        JournalsClient.tracker.setBookmark(bookmark);
    }

    @Override
    protected void init() {
        super.init();

        initEditBox();

        photo = new BookmarkPhoto(minecraft, bookmark);
        runeStringRenderer = new RuneStringRenderer(midX - 61, 165, 13, 14, 10, 4);

        pageButtons.clear();

        pageButtons.add(new ButtonDefinition(b -> chooseIcon(), JournalResources.CHOOSE_ICON));
        pageButtons.add(new ButtonDefinition(b -> takePhoto(), JournalResources.TAKE_PHOTO, JournalResources.HINT_PHOTO, b -> b.active = playerIsNearBookmark()));
        pageButtons.add(new ButtonDefinition(b -> makeMap(), JournalResources.MAKE_MAP, JournalResources.HINT_MAP, b -> b.active = playerCanMakeMap()));

        bottomNavButtons.add(new GuiHelper.ImageButtonDefinition(b -> remove(), JournalResources.NAVIGATION, 20, 0, 18, JournalResources.DELETE_TOOLTIP));
        bottomButtons.add(0, new ButtonDefinition(b -> save(), JournalResources.GO_BACK));

        // add map icon if player has an empty map
        if (playerCanMakeMap()) {
            rightNavButtons.add(new GuiHelper.ImageButtonDefinition(b -> makeMap(), JournalResources.NAVIGATION, 40, 0, 18, JournalResources.MAKE_MAP_TOOLTIP));
        }

        // add take picture icon if player is near the location
        if (playerIsNearBookmark()) {
            rightNavButtons.add(new GuiHelper.ImageButtonDefinition(b -> takePhoto(), JournalResources.NAVIGATION, 80, 0, 18, JournalResources.TAKE_PHOTO_TOOLTIP));
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

        renderTitleIcon(BookmarksClient.getBookmarkIconItem(bookmark));
        renderPhoto(poseStack);
        renderDimensionName(poseStack);
        renderRunes(poseStack, mouseX, mouseY);

        nameEditBox.render(poseStack, mouseX, mouseY, delta);
    }

    protected void renderDimensionName(PoseStack poseStack) {
        int top = 133;
        GuiHelper.drawCenteredString(poseStack, font, dimensionName, midX, top, secondaryColor);
    }

    protected void renderRunes(PoseStack poseStack, int mouseX, int mouseY) {
        var journal = JournalsClient.getJournal().orElse(null);
        if (journal == null) return;

        BlockPos pos = bookmark.getBlockPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int top = 146;

        GuiHelper.drawCenteredString(poseStack, font, new TextComponent(x + " " + y + " " + z), midX, top, secondaryColor);

        // Don't show anything if the player hasn't learned any runes.
        if (JournalHelper.getLearnedRunes(journal).isEmpty()) return;

        // The player must be in the same dimension as the bookmark.
        if (!DimensionHelper.isDimension(minecraft.player.level, bookmark.getDimension())) return;

        runeStringRenderer.render(poseStack, font, bookmark.getRunes());
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

    @Override
    public void onClose() {
        JournalsClient.tracker.setPage(PageTracker.Page.BOOKMARKS);
        super.onClose();
    }

    protected void save() {
        BookmarksClient.SEND_UPDATE_BOOKMARK.send(bookmark);
    }

    protected void remove() {
        BookmarksClient.SEND_REMOVE_BOOKMARK.send(bookmark);
    }

    protected void chooseIcon() {
        minecraft.setScreen(new JournalChooseIconScreen(bookmark));
    }

    protected void makeMap() {
        JournalsClient.CLIENT_SEND_MAKE_MAP.send(bookmark);
        minecraft.setScreen(null);
    }

    protected void takePhoto() {
        JournalsClient.photo.setBookmark(bookmark);
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
