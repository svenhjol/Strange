package svenhjol.strange.feature.travel_journal.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.travel_journal.*;
import svenhjol.strange.feature.travel_journal.TravelJournalNetwork.RequestDeleteBookmark;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

public class BookmarkScreen extends BaseTravelJournalScreen {
    protected Bookmark bookmark;
    protected boolean hasPhoto;
    protected boolean hasMap;
    protected boolean hasPaper;
    protected boolean isNearby;
    protected boolean renderedButtons;
    protected ResourceLocation registeredTexture = null;

    public BookmarkScreen(Bookmark bookmark) {
        super(TextHelper.literal(bookmark.name));
        this.bookmark = bookmark.copy();
        PageTracker.bookmark = bookmark;
        PageTracker.Screen.BOOKMARK.set();
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new Buttons.CloseButton(midX + 5,220, b -> onClose()));
        addRenderableWidget(new Buttons.BackButton(midX - (Buttons.BackButton.WIDTH + 5), 220, this::openBookmarks));

        renderedButtons = false;
        hasPhoto = getScreenshotFile().exists();
        hasMap = hasMap();
        hasPaper = hasPaper();
        isNearby = isNearby();

        initShortcuts();
    }

    @Override
    protected void initShortcuts() {
        super.initShortcuts();
        int yoffset = 91;
        int lineHeight = 17;

        if (hasMap) {
            addRenderableWidget(new SaveToMapShortcutButton(midX + 120, yoffset, b -> {}));
            yoffset += lineHeight;
        }
        if (hasPaper) {
            addRenderableWidget(new SaveToBookmarkShortcutButton(midX + 120, yoffset, b -> {}));
            yoffset += lineHeight;
        }

        yoffset += 70;
        addRenderableWidget(new DeleteShortcutButton(midX + 120, yoffset, b -> delete()));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);

        // Item in top left.
        guiGraphics.renderItem(bookmark.getItemStack(), midX - 108, 20);

        int yoffset = 36;

        if (TravelJournal.showCoordinates) {
            renderCoords(guiGraphics, yoffset);
            yoffset += 16;
        } else {
            renderDimensionName(guiGraphics, yoffset);
            yoffset += 16;
        }

        if (hasPhoto) {
            renderPhoto(guiGraphics, (int)(yoffset * 2.4));
            yoffset += 83;
        }

        if (!renderedButtons) {
            if (!hasPhoto && isNearby) {
                addRenderableWidget(new TakePhotoButton(midX - (TakePhotoButton.WIDTH / 2), yoffset,
                    b -> TravelJournalClient.initPhoto(bookmark)));
                yoffset += 21;
            }

            addRenderableWidget(new ChangeNameButton(midX - (ChangeNameButton.WIDTH / 2), yoffset, this::openChangeNameScreen));
            yoffset += 21;

            addRenderableWidget(new ChangeIconButton(midX - (ChangeIconButton.WIDTH / 2), yoffset, this::openChangeIconScreen));
            yoffset += 21;

            if (hasPhoto && isNearby) {
                addRenderableWidget(new TakeNewPhotoButton(midX - (TakeNewPhotoButton.WIDTH / 2), yoffset,
                    b -> TravelJournalClient.initPhoto(bookmark)));
            }
        }

        renderedButtons = true;
    }

    protected void renderCoords(GuiGraphics guiGraphics, int y) {
        var pos = bookmark.pos;
        var dim = bookmark.dim;
        var str = TravelJournalHelper.getNiceDimensionName(dim) + ": " + TravelJournalHelper.getNiceCoordinates(pos);
        drawCenteredString(guiGraphics, Component.literal(str), midX, y, 0x8a8785, false);
    }

    protected void renderDimensionName(GuiGraphics guiGraphics, int y) {
        var dim = bookmark.dim;
        var str = TravelJournalHelper.getNiceDimensionName(dim);
        drawCenteredString(guiGraphics, Component.literal(str), midX, y, 0x8a8785, false);
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
        if (TravelJournal.showCoordinates
            && mouseX > midX - 100 && mouseX < midX + 100
            && mouseY > 36 && mouseY < 46) {
            var minecraft = Minecraft.getInstance();
            String formattedPos = TravelJournalHelper.getNiceCoordinates(bookmark.pos);
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
        RequestDeleteBookmark.send(bookmark);
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

    static class TakePhotoButton extends Button {
        static int WIDTH = 100;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.TAKE_PHOTO_BUTTON_TEXT;
        public TakePhotoButton(int x, int y, Button.OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    static class TakeNewPhotoButton extends Button {
        static int WIDTH = 100;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.TAKE_NEW_PHOTO_BUTTON_TEXT;
        public TakeNewPhotoButton(int x, int y, Button.OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    static class ChangeNameButton extends Button {
        static int WIDTH = 100;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.CHANGE_NAME_BUTTON_TEXT;
        public ChangeNameButton(int x, int y, Button.OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    static class ChangeIconButton extends Button {
        static int WIDTH = 100;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.CHANGE_ICON_BUTTON_TEXT;
        public ChangeIconButton(int x, int y, Button.OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    static class TakePhotoShortcutButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = TravelJournalResources.PHOTO_BUTTON;
        static Component TEXT = TravelJournalResources.TAKE_PHOTO_BUTTON_TEXT;

        protected TakePhotoShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    static class SaveToBookmarkShortcutButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = TravelJournalResources.SAVE_TO_BOOKMARK_BUTTON;
        static Component TEXT = TravelJournalResources.SAVE_TO_BOOKMARK_BUTTON_TEXT;

        protected SaveToBookmarkShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    static class SaveToMapShortcutButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = TravelJournalResources.SAVE_TO_MAP_BUTTON;
        static Component TEXT = TravelJournalResources.SAVE_TO_MAP_BUTTON_TEXT;

        protected SaveToMapShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    static class DeleteShortcutButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = TravelJournalResources.TRASH_BUTTON;
        static Component TEXT = TravelJournalResources.DELETE_BUTTON_TEXT;

        protected DeleteShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }
}
