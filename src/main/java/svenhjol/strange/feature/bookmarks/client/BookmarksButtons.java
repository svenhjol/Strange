package svenhjol.strange.feature.bookmarks.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import svenhjol.charmony.base.Mods;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.bookmarks.BookmarksResources;

public class BookmarksButtons {
    public static final WidgetSprites BOOKMARKS_BUTTON = makeButton("bookmarks");
    public static final WidgetSprites SAVE_TO_BOOKMARK_BUTTON = makeButton("save_to_bookmark");
    public static final WidgetSprites SAVE_TO_MAP_BUTTON = makeButton("save_to_map");
    public static final WidgetSprites PHOTO_BUTTON = makeButton("photo");

    public static class BookmarksButton extends Button {
        public static int WIDTH = 100;
        public static int HEIGHT = 20;
        static Component TEXT = BookmarksResources.BOOKMARKS_BUTTON_TEXT;

        public BookmarksButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class BookmarksShortcutButton extends ImageButton {
        public static int WIDTH = 20;
        public static int HEIGHT = 18;
        static WidgetSprites SPRITES = BOOKMARKS_BUTTON;
        static Component TEXT = BookmarksResources.BOOKMARKS_BUTTON_TEXT;

        public BookmarksShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    public static class NewWhenEmptyButton extends Button {
        public static int WIDTH = 100;
        public static int HEIGHT = 20;
        static Component TEXT = BookmarksResources.NEW_BOOKMARK_BUTTON_TEXT;

        public NewWhenEmptyButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class NewBookmarkButton extends Button {
        public static int WIDTH = 110;
        public static int HEIGHT = 20;
        static Component TEXT = BookmarksResources.NEW_BOOKMARK_BUTTON_TEXT;

        public NewBookmarkButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class TakePhotoButton extends Button {
        public static int WIDTH = 100;
        public static int HEIGHT = 20;
        static Component TEXT = BookmarksResources.TAKE_PHOTO_BUTTON_TEXT;
        public TakePhotoButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class TakeNewPhotoButton extends Button {
        public static int WIDTH = 100;
        public static int HEIGHT = 20;
        static Component TEXT = BookmarksResources.TAKE_NEW_PHOTO_BUTTON_TEXT;
        public TakeNewPhotoButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class ChangeNameButton extends Button {
        public static int WIDTH = 100;
        public static int HEIGHT = 20;
        static Component TEXT = BookmarksResources.CHANGE_NAME_BUTTON_TEXT;
        public ChangeNameButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class ChangeIconButton extends Button {
        public static int WIDTH = 100;
        public static int HEIGHT = 20;
        static Component TEXT = BookmarksResources.CHANGE_ICON_BUTTON_TEXT;
        public ChangeIconButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class TakePhotoShortcutButton extends ImageButton {
        public static int WIDTH = 20;
        public static int HEIGHT = 18;
        static WidgetSprites SPRITES = PHOTO_BUTTON;
        static Component TEXT = BookmarksResources.TAKE_PHOTO_BUTTON_TEXT;

        public TakePhotoShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    public static class SaveToBookmarkShortcutButton extends ImageButton {
        public static int WIDTH = 20;
        public static int HEIGHT = 18;
        static WidgetSprites SPRITES = SAVE_TO_BOOKMARK_BUTTON;
        static Component TEXT = BookmarksResources.SAVE_TO_BOOKMARK_BUTTON_TEXT;

        public SaveToBookmarkShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    public static class SaveToMapShortcutButton extends ImageButton {
        public static int WIDTH = 20;
        public static int HEIGHT = 18;
        static WidgetSprites SPRITES = SAVE_TO_MAP_BUTTON;
        static Component TEXT = BookmarksResources.SAVE_TO_MAP_BUTTON_TEXT;

        public SaveToMapShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    static WidgetSprites makeButton(String name) {
        var instance = Mods.client(Strange.ID);

        return new WidgetSprites(
            instance.id("widget/bookmarks/" + name + "_button"),
            instance.id("widget/bookmarks/" + name + "_button_highlighted"));
    }
}
