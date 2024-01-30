package svenhjol.strange.feature.learned_runes.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import svenhjol.charmony.base.Mods;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.learned_runes.LearnedRunesResources;

public class LearnedRunesButtons {
    public static final WidgetSprites LEARNED_BUTTON = makeButton("runes");

    public static class LearnedButton extends Button {
        public static int WIDTH = 100;
        public static int HEIGHT = 20;
        static Component TEXT = LearnedRunesResources.LEARNED_BUTTON_TEXT;

        public LearnedButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class LearnedShortcutButton extends ImageButton {
        public static int WIDTH = 20;
        public static int HEIGHT = 18;
        static WidgetSprites SPRITES = LEARNED_BUTTON;
        static Component TEXT = LearnedRunesResources.LEARNED_BUTTON_TEXT;

        public LearnedShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    static WidgetSprites makeButton(String name) {
        var instance = Mods.client(Strange.ID);

        return new WidgetSprites(
            instance.id("widget/learned_runes/" + name + "_button"),
            instance.id("widget/learned_runes/" + name + "_button_highlighted"));
    }
}
