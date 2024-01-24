package svenhjol.strange.feature.quests.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.quests.QuestHelper;

public class QuestButtons {
    public static class AcceptButton extends Button {
        static int WIDTH = 70;
        static int HEIGHT = 20;
        static Component TEXT = QuestResources.ACCEPT_BUTTON_TEXT;

        public AcceptButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);

            // If player is at max quests, disable the button and show a tooltip.
            var player = Minecraft.getInstance().player;
            if (player != null && QuestHelper.hasMaxQuests(player)) {
                active = false;
                setTooltip(Tooltip.create(TextHelper.translatable("gui.strange.quests.too_many_quests")));
            }
        }
    }

    public static class QuestsButton extends Button {
        public static int WIDTH = 110;
        public static int HEIGHT = 20;

        static Component TEXT = QuestResources.QUEST_BUTTON_TEXT;
        public QuestsButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class ScrollImageButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;

        public ScrollImageButton(WidgetSprites sprites, int x, int y, OnPress onPress, Component tooltip) {
            super(x, y, WIDTH, HEIGHT, sprites, onPress);
            setTooltip(Tooltip.create(tooltip));
        }
    }

    public static class AbandonShortcutButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = QuestResources.TRASH_BUTTON;
        static Component TEXT = QuestResources.ABANDON_BUTTON_TEXT;

        public AbandonShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }
}
