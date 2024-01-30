package svenhjol.strange.feature.quests.client;

import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class QuestResources {
    public static final Component QUEST_BUTTON_TEXT = TextHelper.translatable("gui.strange.quests.quests");
    public static final Component ARTIFACT_TEXT = TextHelper.translatable("gui.strange.quests.artifact");
    public static final Component ARTIFACT_REQUIREMENT_TEXT = TextHelper.translatable("gui.strange.quests.artifact_requirement");
    public static final Component ARTIFACT_REWARD_TEXT = TextHelper.translatable("gui.strange.quests.artifact_reward");
    public static final Component GATHER_TEXT = TextHelper.translatable("gui.strange.quests.gather");
    public static final Component GATHER_REQUIREMENT_TEXT = TextHelper.translatable("gui.strange.quests.gather_requirement");
    public static final Component GATHER_REWARD_TEXT = TextHelper.translatable("gui.strange.quests.gather_reward");
    public static final Component HUNT_TEXT = TextHelper.translatable("gui.strange.quests.hunt");
    public static final Component HUNT_REQUIREMENT_TEXT = TextHelper.translatable("gui.strange.quests.hunt_requirement");
    public static final Component HUNT_REWARD_TEXT = TextHelper.translatable("gui.strange.quests.hunt_reward");
    public static final Component BATTLE_TEXT = TextHelper.translatable("gui.strange.quests.battle");
    public static final Component BATTLE_REQUIREMENT_TEXT = TextHelper.translatable("gui.strange.quests.battle_requirement");
    public static final Component BATTLE_REWARD_TEXT = TextHelper.translatable("gui.strange.quests.battle_reward");
    public static final Component QUEST_INFO_TEXT = TextHelper.translatable("gui.strange.quests.quest_info");
    public static final Component ACCEPT_BUTTON_TEXT = TextHelper.translatable("gui.strange.quests.accept");
    public static final Component ABANDON_BUTTON_TEXT = TextHelper.translatable("gui.strange.quests.abandon");
    public static final ResourceLocation NOVICE_SCROLL = new ResourceLocation(Strange.ID, "scroll/novice_scroll");
    public static final ResourceLocation APPRENTICE_SCROLL = new ResourceLocation(Strange.ID, "scroll/apprentice_scroll");
    public static final ResourceLocation JOURNEYMAN_SCROLL = new ResourceLocation(Strange.ID, "scroll/journeyman_scroll");
    public static final ResourceLocation EXPERT_SCROLL = new ResourceLocation(Strange.ID, "scroll/expert_scroll");
    public static final ResourceLocation MASTER_SCROLL = new ResourceLocation(Strange.ID, "scroll/master_scroll");
    public static final ResourceLocation TICK = new ResourceLocation(Strange.ID, "quest/tick");
    public static final String QUEST_OFFERS_TITLE_KEY = "gui.strange.quests.quest_offers";
    public static final String EPIC_QUEST_TITLE_KEY = "gui.strange.quests.epic_quest_title";
    public static final String EPIC_QUEST_TITLE_WITH_PROFESSION_KEY = "gui.strange.quests.epic_quest_title_with_profession";
    public static final String QUEST_TITLE_KEY = "gui.strange.quests.quest_title";
    public static final String QUEST_TITLE_WITH_PROFESSION_KEY = "gui.strange.quests.quest_title_with_profession";
    public static final String AMOUNT_KEY = "gui.strange.quests.amount";
    public static final String REWARD_LEVELS_KEY = "gui.strange.quests.reward_levels";
    public static final String SATISFIED_KEY = "gui.strange.quests.satisfied";
    public static final Map<Integer, ResourceLocation> LEVEL_TO_SCROLL = new HashMap<>();
    public static final Map<Integer, WidgetSprites> LEVEL_TO_SCROLL_BUTTON = new HashMap<>();
    public static final Map<EntityType<?>, ResourceLocation> MOB_SPRITES = new HashMap<>();
    public static final Map<ResourceLocation, ResourceLocation> LOOT_SPRITES = new HashMap<>();
    public static final WidgetSprites NOVICE_SCROLL_BUTTON = makeButton("novice_scroll");
    public static final WidgetSprites APPRENTICE_SCROLL_BUTTON = makeButton("apprentice_scroll");
    public static final WidgetSprites JOURNEYMAN_SCROLL_BUTTON = makeButton("journeyman_scroll");
    public static final WidgetSprites EXPERT_SCROLL_BUTTON = makeButton("expert_scroll");
    public static final WidgetSprites MASTER_SCROLL_BUTTON = makeButton("master_scroll");
    public static final WidgetSprites TRASH_BUTTON = makeButton("trash");
    public static final WidgetSprites ACCEPT_BUTTON = makeButton("accept");

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
