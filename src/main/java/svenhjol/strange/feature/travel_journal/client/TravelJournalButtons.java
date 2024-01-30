package svenhjol.strange.feature.travel_journal.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;

public class TravelJournalButtons {
    public static class CloseButton extends Button {
        public static int WIDTH = 110;
        public static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.CLOSE_BUTTON_TEXT;

        public CloseButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class BackButton extends Button {
        public static int WIDTH = 110;
        public static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.BACK_BUTTON_TEXT;

        public BackButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class SaveButton extends Button {
        public static int WIDTH = 110;
        public static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.SAVE_BUTTON_TEXT;

        public SaveButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class CancelButton extends Button {
        public static int WIDTH = 110;
        public static int HEIGHT = 20;
        static Component TEXT = TravelJournalResources.CANCEL_BUTTON_TEXT;

        public CancelButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class EditButton extends Button {
        public static int HEIGHT = 20;

        public EditButton(int x, int y, int width, OnPress onPress, Component text) {
            super(x, y, width, HEIGHT, text, onPress, DEFAULT_NARRATION);
        }
    }

    public static class HomeShortcutButton extends ImageButton {
        public static int WIDTH = 20;
        public static int HEIGHT = 18;
        static WidgetSprites SPRITES = TravelJournalResources.HOME_BUTTON;
        static Component TEXT = TravelJournalResources.HOME_BUTTON_TEXT;

        public HomeShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    public static class NextPageButton extends ImageButton {
        public static int WIDTH = 20;
        public static int HEIGHT = 19;
        static WidgetSprites SPRITES = TravelJournalResources.NEXT_PAGE_BUTTON;
        static Component TEXT = TravelJournalResources.NEXT_PAGE_BUTTON_TEXT;

        public NextPageButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    public static class PreviousPageButton extends ImageButton {
        public static int WIDTH = 20;
        public static int HEIGHT = 19;
        static WidgetSprites SPRITES = TravelJournalResources.PREVIOUS_PAGE_BUTTON;
        static Component TEXT = TravelJournalResources.PREVIOUS_PAGE_BUTTON_TEXT;

        public PreviousPageButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    public static class DeleteShortcutButton extends ImageButton {
        public static int WIDTH = 20;
        public static int HEIGHT = 18;
        static WidgetSprites SPRITES = TravelJournalResources.TRASH_BUTTON;
        static Component TEXT = TravelJournalResources.DELETE_BUTTON_TEXT;

        public DeleteShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }
}
