package svenhjol.strange.feature.travel_journal.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;

public class TravelJournalButtons {
    public static class CloseButton extends Button {
        static int WIDTH = 110;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.CLOSE_BUTTON_TEXT;

        public CloseButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class BackButton extends Button {
        static int WIDTH = 110;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.BACK_BUTTON_TEXT;

        public BackButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class SaveButton extends Button {
        static int WIDTH = 110;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.SAVE_BUTTON_TEXT;

        public SaveButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class CancelButton extends Button {
        static int WIDTH = 110;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.CANCEL_BUTTON_TEXT;

        public CancelButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class EditButton extends Button {
        static int HEIGHT = 20;

        protected EditButton(int x, int y, int width, OnPress onPress, Component text) {
            super(x, y, width, HEIGHT, text, onPress, DEFAULT_NARRATION);
        }
    }

    public static class HomeShortcutButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = TravelJournalResources.HOME_BUTTON;
        static Component TEXT = TravelJournalResources.HOME_BUTTON_TEXT;

        protected HomeShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    public static class BookmarksShortcutButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = TravelJournalResources.BOOKMARKS_BUTTON;
        static Component TEXT = TravelJournalResources.BOOKMARKS_BUTTON_TEXT;

        public BookmarksShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    public static class LearnedShortcutButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = TravelJournalResources.LEARNED_BUTTON;
        static Component TEXT = TravelJournalResources.LEARNED_BUTTON_TEXT;

        public LearnedShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    public static class QuestsShortcutButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = TravelJournalResources.QUESTS_BUTTON;
        static Component TEXT = TravelJournalResources.QUESTS_BUTTON_TEXT;

        public QuestsShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    public static class NextPageButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 19;
        static WidgetSprites SPRITES = TravelJournalResources.NEXT_PAGE_BUTTON;
        static Component TEXT = TravelJournalResources.NEXT_PAGE_BUTTON_TEXT;

        public NextPageButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    public static class PreviousPageButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 19;
        static WidgetSprites SPRITES = TravelJournalResources.PREVIOUS_PAGE_BUTTON;
        static Component TEXT = TravelJournalResources.PREVIOUS_PAGE_BUTTON_TEXT;

        public PreviousPageButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    public static class TakePhotoButton extends Button {
        static int WIDTH = 100;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.TAKE_PHOTO_BUTTON_TEXT;
        public TakePhotoButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class TakeNewPhotoButton extends Button {
        static int WIDTH = 100;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.TAKE_NEW_PHOTO_BUTTON_TEXT;
        public TakeNewPhotoButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class ChangeNameButton extends Button {
        static int WIDTH = 100;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.CHANGE_NAME_BUTTON_TEXT;
        public ChangeNameButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class ChangeIconButton extends Button {
        static int WIDTH = 100;
        static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.CHANGE_ICON_BUTTON_TEXT;
        public ChangeIconButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class TakePhotoShortcutButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = TravelJournalResources.PHOTO_BUTTON;
        static Component TEXT = TravelJournalResources.TAKE_PHOTO_BUTTON_TEXT;

        protected TakePhotoShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    public static class SaveToBookmarkShortcutButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = TravelJournalResources.SAVE_TO_BOOKMARK_BUTTON;
        static Component TEXT = TravelJournalResources.SAVE_TO_BOOKMARK_BUTTON_TEXT;

        protected SaveToBookmarkShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    public static class SaveToMapShortcutButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = TravelJournalResources.SAVE_TO_MAP_BUTTON;
        static Component TEXT = TravelJournalResources.SAVE_TO_MAP_BUTTON_TEXT;

        protected SaveToMapShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    public static class DeleteShortcutButton extends ImageButton {
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
