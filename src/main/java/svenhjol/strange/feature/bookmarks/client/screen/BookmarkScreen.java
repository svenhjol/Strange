package svenhjol.strange.feature.bookmarks.client.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.bookmarks.*;
import svenhjol.strange.feature.bookmarks.client.BookmarksButtons;
import svenhjol.strange.feature.bookmarks.client.BookmarksButtons.*;
import svenhjol.strange.feature.travel_journal.PageTracker;
import svenhjol.strange.feature.travel_journal.client.TravelJournalButtons.BackButton;
import svenhjol.strange.feature.travel_journal.client.TravelJournalButtons.CloseButton;
import svenhjol.strange.feature.travel_journal.client.TravelJournalButtons.DeleteShortcutButton;
import svenhjol.strange.feature.travel_journal.client.screen.TravelJournalScreen;
import svenhjol.strange.helper.GuiHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

public class BookmarkScreen extends TravelJournalScreen {
    protected Bookmark bookmark;
    protected boolean hasPhoto;
    protected boolean hasMap;
    protected boolean hasPaper;
    protected boolean isNearby;
    protected boolean renderedButtons;
    protected ResourceLocation registeredTexture = null;

    public BookmarkScreen(Bookmark bookmark) {
        super(TextHelper.literal(bookmark.name));
        setBookmark(bookmark);
        PageTracker.set(() -> new BookmarkScreen(bookmark));
    }

    @Override
    protected void init() {
        super.init();

        // Add footer buttons
        addRenderableWidget(new CloseButton(midX + 5,220, b -> onClose()));
        addRenderableWidget(new BackButton(midX - (BackButton.WIDTH + 5), 220, BookmarksClient::openBookmarksScreen));

        renderedButtons = false;
        hasPhoto = getScreenshotFile().exists();
        hasMap = hasMap();
        hasPaper = hasPaper();
        isNearby = isNearby();

        updateBookmark();
        initShortcuts();
    }

    @Override
    protected void initShortcuts() {
        super.initShortcuts();
        int yo = 91;
        int lineHeight = 17;

        if (hasMap) {
            addRenderableWidget(new SaveToMapShortcutButton(midX + 120, yo, b -> {}));
            yo += lineHeight;
        }

        if (hasPaper) {
            addRenderableWidget(new SaveToBookmarkShortcutButton(midX + 120, yo, b -> {}));
        }

        addRenderableWidget(new DeleteShortcutButton(midX + 120, 161, b -> delete()));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);

        // Item in top left.
        guiGraphics.renderItem(bookmark.getItemStack(), midX - 108, 20);

        int yo = 36;

        if (Bookmarks.showCoordinates) {
            renderCoords(guiGraphics, yo);
            yo += 16;
        } else {
            renderDimensionName(guiGraphics, yo);
            yo += 16;
        }

        if (hasPhoto) {
            renderPhoto(guiGraphics, (int)(yo * 2.4));
            yo += 83;
        }

        renderButtons(yo);
        checkValid();
    }

    /**
     * Only run the first time round the render loop
     */
    protected void renderButtons(int yo) {
        if (!renderedButtons) {
            if (!hasPhoto && isNearby) {
                addRenderableWidget(new TakePhotoButton(midX - (TakePhotoButton.WIDTH / 2), yo,
                    b -> BookmarksClient.initPhoto(bookmark)));
                yo += 21;
            }

            addRenderableWidget(new ChangeNameButton(midX - (ChangeNameButton.WIDTH / 2), yo, this::openChangeNameScreen));
            yo += 21;

            addRenderableWidget(new ChangeIconButton(midX - (BookmarksButtons.ChangeIconButton.WIDTH / 2), yo, this::openChangeIconScreen));
            yo += 21;

            if (hasPhoto && isNearby) {
                addRenderableWidget(new TakeNewPhotoButton(midX - (TakeNewPhotoButton.WIDTH / 2), yo,
                    b -> BookmarksClient.initPhoto(bookmark)));
            }
        }

        renderedButtons = true;
    }

    protected void renderCoords(GuiGraphics guiGraphics, int y) {
        var pos = bookmark.pos;
        var dim = bookmark.dim;
        var str = BookmarksHelper.getNiceDimensionName(dim) + ": " + BookmarksHelper.getNiceCoordinates(pos);
        GuiHelper.drawCenteredString(guiGraphics, font, Component.literal(str), midX, y, 0x8a8785, false);
    }

    protected void renderDimensionName(GuiGraphics guiGraphics, int y) {
        var dim = bookmark.dim;
        var str = BookmarksHelper.getNiceDimensionName(dim);
        GuiHelper.drawCenteredString(guiGraphics, font, Component.literal(str), midX, y, 0x8a8785, false);
    }

    protected void renderPhoto(GuiGraphics guiGraphics, int y) {
        if (!hasPhoto) {
            return;
        }

        // Try and load the screenshot if the texture is not cached.
        if (registeredTexture == null) {
            var minecraft = Minecraft.getInstance();

            try {
                var screenshotFile = getScreenshotFile();
                var raf = new RandomAccessFile(screenshotFile, "r");

                //noinspection ConstantValue
                if (raf != null) {
                    raf.close();
                }

                var stream = new FileInputStream(screenshotFile);
                var screenshot = NativeImage.read(stream);
                var dynamicTexture = new DynamicTexture(screenshot);
                registeredTexture = minecraft.getTextureManager()
                    .register("screenshot", dynamicTexture);

                stream.close();

                if (registeredTexture == null) {
                    throw new Exception("Problem with screenshot texture / registered texture");
                }

            } catch (Exception e) {

                log().error(this.getClass(), "Failure loading screenshot");
                hasPhoto = false;
                registeredTexture = null;
            }
        }

        if (registeredTexture != null) {
            var pose = guiGraphics.pose();
            RenderSystem.setShaderTexture(0, registeredTexture);
            pose.pushPose();
            pose.scale(0.66F, 0.4F, 1.0F);
            guiGraphics.blit(registeredTexture, (int)(midX / 0.66f) - 114, y, 0, 0, 228, 200);
            pose.popPose();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Bookmarks.showCoordinates
            && mouseX > midX - 100 && mouseX < midX + 100
            && mouseY > 36 && mouseY < 46) {
            var minecraft = Minecraft.getInstance();
            String formattedPos = BookmarksHelper.getNiceCoordinates(bookmark.pos);
            String chatMessage;

            if (minecraft.player != null && minecraft.player.getAbilities().instabuild) {
                chatMessage = "/tp " + formattedPos;
            } else {
                chatMessage = bookmark.name + ": " + formattedPos;
            }

            minecraft.setScreen(new ChatScreen(chatMessage));
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected File getScreenshotFile() {
        var minecraft = Minecraft.getInstance();
        var screenshotsDirectory = new File(minecraft.gameDirectory, "screenshots");
        return new File(screenshotsDirectory, bookmark.id + ".png");
    }

    protected void openChangeNameScreen(Button button) {
        Minecraft.getInstance().setScreen(new ChangeBookmarkNameScreen(bookmark));
    }

    protected void openChangeIconScreen(Button button) {
        Minecraft.getInstance().setScreen(new ChangeBookmarkIconScreen(bookmark));
    }

    protected void delete() {
        BookmarksNetwork.RequestDeleteBookmark.send(bookmark);
    }

    protected boolean isNearby() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return false;
        }

        var dist = minecraft.player.blockPosition().distManhattan(bookmark.pos);
        return dist < 8;
    }

    protected boolean hasMap() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return false;
        }

        return minecraft.player.getInventory().contains(new ItemStack(Items.MAP));
    }

    protected boolean hasPaper() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return false;
        }

        return minecraft.player.getInventory().contains(new ItemStack(Items.PAPER));
    }

    /**
     * Update the bookmark from the player's most recently synced set.
     * This prevents bookmark data from going stale between screen views.
     */
    protected void updateBookmark() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            Bookmarks.getBookmarks(minecraft.player).get(bookmark.id).ifPresent(this::setBookmark);
        }
    }

    protected void setBookmark(Bookmark bookmark) {
        this.bookmark = bookmark.copy();
    }

    protected void checkValid() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player != null && !Bookmarks.getBookmarks(minecraft.player).exists(this.bookmark)) {
            minecraft.setScreen(new BookmarksScreen());
        }
    }
}
