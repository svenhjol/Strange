package svenhjol.strange.feature.travel_journals.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import svenhjol.strange.Strange;

public class Buttons {
    public static final WidgetSprites EXPORT_MAP_BUTTON = makeButton("map");
    public static final WidgetSprites EXPORT_PAGE_BUTTON = makeButton("page");

    public static class NewWhenEmptyButton extends Button {
        public static int WIDTH = 100;
        public static int HEIGHT = 20;
        static Component TEXT = Resources.NEW_BOOKMARK;

        public NewWhenEmptyButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class NewBookmarkButton extends Button {
        public static int WIDTH = 110;
        public static int HEIGHT = 20;
        static Component TEXT = Resources.NEW_BOOKMARK;

        public NewBookmarkButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class ExportMapButton extends ImageButton {
        public static int WIDTH = 20;
        public static int HEIGHT = 18;
        static WidgetSprites SPRITES = EXPORT_MAP_BUTTON;
        static Component TEXT = Resources.EXPORT_MAP;

        public ExportMapButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    public static class ExportPageButton extends ImageButton {
        public static int WIDTH = 20;
        public static int HEIGHT = 18;
        static WidgetSprites SPRITES = EXPORT_PAGE_BUTTON;
        static Component TEXT = Resources.EXPORT_PAGE;

        public ExportPageButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    private static WidgetSprites makeButton(String name) {
        return new WidgetSprites(
            Strange.id("widget/travel_journals/" + name + "_button"),
            Strange.id("widget/travel_journals/" + name + "_button_highlighted"));
    }
}
