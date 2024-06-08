package svenhjol.strange.feature.core.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import svenhjol.strange.Strange;

public final class CoreButtons {
    public static final WidgetSprites NEXT_PAGE_BUTTON = makeButton("next_page");
    public static final WidgetSprites PREVIOUS_PAGE_BUTTON = makeButton("previous_page");


    public static class CloseButton extends Button {
        public static int WIDTH = 110;
        public static int HEIGHT = 20;
        static Component TEXT = CoreResources.CLOSE;

        public CloseButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class BackButton extends Button {
        public static int WIDTH = 100;
        public static int HEIGHT = 20;
        static Component TEXT = CoreResources.BACK;

        public BackButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class SaveButton extends Button {
        public static int WIDTH = 100;
        public static int HEIGHT = 20;
        static Component TEXT = CoreResources.SAVE;

        public SaveButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class CancelButton extends Button {
        public static int WIDTH = 100;
        public static int HEIGHT = 20;
        static Component TEXT = CoreResources.CANCEL;

        public CancelButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class DeleteButton extends Button {
        public static int WIDTH = 100;
        public static int HEIGHT = 20;
        static Component TEXT = CoreResources.DELETE;

        public DeleteButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class EditButton extends Button {
        public static int HEIGHT = 20;

        public EditButton(int x, int y, int width, OnPress onPress, Component text) {
            super(x, y, width, HEIGHT, text, onPress, DEFAULT_NARRATION);
        }
    }
    
    public static class NextPageButton extends ImageButton {
        public static int WIDTH = 20;
        public static int HEIGHT = 19;
        static WidgetSprites SPRITES = NEXT_PAGE_BUTTON;
        static Component TEXT = CoreResources.NEXT_PAGE;

        public NextPageButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    public static class PreviousPageButton extends ImageButton {
        public static int WIDTH = 20;
        public static int HEIGHT = 19;
        static WidgetSprites SPRITES = PREVIOUS_PAGE_BUTTON;
        static Component TEXT = CoreResources.PREVIOUS_PAGE;

        public PreviousPageButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    static WidgetSprites makeButton(String name) {
        return new WidgetSprites(
            Strange.id("widget/core/" + name + "_button"),
            Strange.id("widget/core/" + name + "_button_highlighted"));
    }
}
