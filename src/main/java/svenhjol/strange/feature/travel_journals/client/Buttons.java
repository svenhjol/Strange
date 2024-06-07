package svenhjol.strange.feature.travel_journals.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class Buttons {
    public static class BookmarksButton extends Button {
        public static int WIDTH = 100;
        public static int HEIGHT = 20;
        static Component TEXT = Resources.BOOKMARKS_BUTTON_TEXT;

        public BookmarksButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class NewWhenEmptyButton extends Button {
        public static int WIDTH = 100;
        public static int HEIGHT = 20;
        static Component TEXT = Resources.NEW_BOOKMARK_BUTTON_TEXT;

        public NewWhenEmptyButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class NewBookmarkButton extends Button {
        public static int WIDTH = 110;
        public static int HEIGHT = 20;
        static Component TEXT = Resources.NEW_BOOKMARK_BUTTON_TEXT;

        public NewBookmarkButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }
}
