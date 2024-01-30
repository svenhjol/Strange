package svenhjol.strange.feature.quests.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.quests.QuestsHelper;
import svenhjol.strange.feature.quests.QuestsResources;

import java.util.HashMap;
import java.util.Map;

public class QuestsButtons {
    public static final WidgetSprites QUESTS_BUTTON = makeButton("quests");
    public static final WidgetSprites NOVICE_SCROLL_BUTTON = makeButton("novice_scroll");
    public static final WidgetSprites APPRENTICE_SCROLL_BUTTON = makeButton("apprentice_scroll");
    public static final WidgetSprites JOURNEYMAN_SCROLL_BUTTON = makeButton("journeyman_scroll");
    public static final WidgetSprites EXPERT_SCROLL_BUTTON = makeButton("expert_scroll");
    public static final WidgetSprites MASTER_SCROLL_BUTTON = makeButton("master_scroll");
    public static final WidgetSprites TRASH_BUTTON = makeButton("trash");
    public static final WidgetSprites ACCEPT_BUTTON = makeButton("accept");
    public static final Map<Integer, WidgetSprites> LEVEL_TO_SCROLL_BUTTON = new HashMap<>();

    public static class QuestsShortcutButton extends ImageButton {
        public static int WIDTH = 20;
        public static int HEIGHT = 18;
        static WidgetSprites SPRITES = QUESTS_BUTTON;
        static Component TEXT = QuestsResources.QUESTS_BUTTON_TEXT;

        public QuestsShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    public static class QuestsButton extends Button {
        public static int WIDTH = 100;
        public static int HEIGHT = 20;
        static Component TEXT = QuestsResources.QUESTS_BUTTON_TEXT;

        public QuestsButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, TEXT, onPress, DEFAULT_NARRATION);
        }
    }

    public static class VillagerQuestsButton extends Button {
        public static int WIDTH = 110;
        public static int HEIGHT = 20;

        static Component TEXT = QuestsResources.VILLAGER_QUESTS_BUTTON_TEXT;
        public VillagerQuestsButton(int x, int y, OnPress onPress) {
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

    public static class AcceptImageButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = ACCEPT_BUTTON;
        static Component TEXT = QuestsResources.ACCEPT_BUTTON_TEXT;

        public AcceptImageButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);

            // If player is at max quests, disable the button and show a tooltip.
            var player = Minecraft.getInstance().player;
            if (player != null && QuestsHelper.hasMaxQuests(player)) {
                active = false;
                setTooltip(Tooltip.create(TextHelper.translatable("gui.strange.quests.too_many_quests")));
                return;
            }

            setTooltip(Tooltip.create(TEXT));
        }
    }

    public static class AbandonShortcutButton extends ImageButton {
        static int WIDTH = 20;
        static int HEIGHT = 18;
        static WidgetSprites SPRITES = TRASH_BUTTON;
        static Component TEXT = QuestsResources.ABANDON_BUTTON_TEXT;

        public AbandonShortcutButton(int x, int y, OnPress onPress) {
            super(x, y, WIDTH, HEIGHT, SPRITES, onPress);
            setTooltip(Tooltip.create(TEXT));
        }
    }

    static WidgetSprites makeButton(String name) {
        var instance = Mods.client(Strange.ID);

        return new WidgetSprites(
            instance.id("widget/quests/" + name + "_button"),
            instance.id("widget/quests/" + name + "_button_highlighted"));
    }

    static {
        LEVEL_TO_SCROLL_BUTTON.put(1, NOVICE_SCROLL_BUTTON);
        LEVEL_TO_SCROLL_BUTTON.put(2, APPRENTICE_SCROLL_BUTTON);
        LEVEL_TO_SCROLL_BUTTON.put(3, JOURNEYMAN_SCROLL_BUTTON);
        LEVEL_TO_SCROLL_BUTTON.put(4, EXPERT_SCROLL_BUTTON);
        LEVEL_TO_SCROLL_BUTTON.put(5, MASTER_SCROLL_BUTTON);
    }
}
