package svenhjol.strange.feature.quests.client;

import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class QuestResources {
    public static final Component QUEST_BUTTON_TEXT = TextHelper.translatable("gui.strange.quests.quests");
    public static final Component GATHER_REQUIREMENT_TEXT = TextHelper.translatable("gui.strange.quests.gather_requirement");
    public static final Component GATHER_REWARD_TEXT = TextHelper.translatable("gui.strange.quests.gather_reward");
    public static final Component UPDATE_QUEST_TEXT = TextHelper.translatable("gui.strange.quests.update_quest");
    public static final Component ACCEPT_QUEST_BUTTON_TEXT = TextHelper.translatable("gui.strange.quests.accept_quest");
    public static final Component ABANDON_QUEST_BUTTON_TEXT = TextHelper.translatable("gui.strange.quests.abandon_quest");
    public static final ResourceLocation NOVICE_SCROLL = new ResourceLocation(Strange.ID, "scroll/novice_scroll");
    public static final ResourceLocation APPRENTICE_SCROLL = new ResourceLocation(Strange.ID, "scroll/apprentice_scroll");
    public static final ResourceLocation JOURNEYMAN_SCROLL = new ResourceLocation(Strange.ID, "scroll/journeyman_scroll");
    public static final ResourceLocation EXPERT_SCROLL = new ResourceLocation(Strange.ID, "scroll/expert_scroll");
    public static final ResourceLocation MASTER_SCROLL = new ResourceLocation(Strange.ID, "scroll/master_scroll");
    public static final ResourceLocation TICK = new ResourceLocation(Strange.ID, "quest/tick");
    public static final String QUEST_OFFERS_TITLE_KEY = "gui.strange.quests.quest_offers";
    public static final String QUEST_TITLE_KEY = "gui.strange.quests.quest_title";
    public static final Map<Integer, ResourceLocation> LEVEL_TO_SCROLL = new HashMap<>();
    public static final Map<Integer, WidgetSprites> LEVEL_TO_SCROLL_BUTTON = new HashMap<>();
    public static final WidgetSprites NOVICE_SCROLL_BUTTON = makeButton("novice_scroll");
    public static final WidgetSprites APPRENTICE_SCROLL_BUTTON = makeButton("apprentice_scroll");
    public static final WidgetSprites JOURNEYMAN_SCROLL_BUTTON = makeButton("journeyman_scroll");
    public static final WidgetSprites EXPERT_SCROLL_BUTTON = makeButton("expert_scroll");
    public static final WidgetSprites MASTER_SCROLL_BUTTON = makeButton("master_scroll");

    static {
        LEVEL_TO_SCROLL.put(1, NOVICE_SCROLL);
        LEVEL_TO_SCROLL.put(2, APPRENTICE_SCROLL);
        LEVEL_TO_SCROLL.put(3, JOURNEYMAN_SCROLL);
        LEVEL_TO_SCROLL.put(4, EXPERT_SCROLL);
        LEVEL_TO_SCROLL.put(5, MASTER_SCROLL);

        LEVEL_TO_SCROLL_BUTTON.put(1, NOVICE_SCROLL_BUTTON);
        LEVEL_TO_SCROLL_BUTTON.put(2, APPRENTICE_SCROLL_BUTTON);
        LEVEL_TO_SCROLL_BUTTON.put(3, JOURNEYMAN_SCROLL_BUTTON);
        LEVEL_TO_SCROLL_BUTTON.put(4, EXPERT_SCROLL_BUTTON);
        LEVEL_TO_SCROLL_BUTTON.put(5, MASTER_SCROLL_BUTTON);
    }

    static WidgetSprites makeButton(String name) {
        var instance = Mods.client(Strange.ID);

        return new WidgetSprites(
            instance.id("widget/quests/" + name + "_button"),
            instance.id("widget/quests/" + name + "_button_highlighted"));
    }
}
