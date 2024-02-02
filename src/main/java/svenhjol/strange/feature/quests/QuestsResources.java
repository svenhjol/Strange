package svenhjol.strange.feature.quests;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class QuestsResources {
    public static final Component QUESTS_TITLE = TextHelper.translatable("gui.strange.quests.quests");
    public static final Component QUESTS_BUTTON_TEXT = TextHelper.translatable("gui.strange.quests.quests");
    public static final Component NO_QUESTS_TEXT = TextHelper.translatable("gui.strange.quests.no_quests");
    public static final Component VILLAGER_QUESTS_BUTTON_TEXT = TextHelper.translatable("gui.strange.quests.villager_quests");
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
    public static final ResourceLocation STAR = new ResourceLocation(Strange.ID, "quest/star");
    public static final String QUEST_OFFERS_TITLE_KEY = "gui.strange.quests.quest_offers";
    public static final String EPIC_QUEST_TITLE_KEY = "gui.strange.quests.epic_quest_title";
    public static final String EPIC_QUEST_TITLE_WITH_PROFESSION_KEY = "gui.strange.quests.epic_quest_title_with_profession";
    public static final String QUEST_TITLE_KEY = "gui.strange.quests.quest_title";
    public static final String QUEST_TITLE_WITH_PROFESSION_KEY = "gui.strange.quests.quest_title_with_profession";
    public static final String AMOUNT_KEY = "gui.strange.quests.amount";
    public static final String REWARD_LEVELS_KEY = "gui.strange.quests.reward_levels";
    public static final String SATISFIED_KEY = "gui.strange.quests.satisfied";
    public static final Map<Integer, ResourceLocation> LEVEL_TO_SCROLL = new HashMap<>();
    public static final Map<EntityType<?>, ResourceLocation> MOB_SPRITES = new HashMap<>();
    public static final Map<ResourceLocation, ResourceLocation> LOOT_SPRITES = new HashMap<>();

    static {
        LEVEL_TO_SCROLL.put(1, NOVICE_SCROLL);
        LEVEL_TO_SCROLL.put(2, APPRENTICE_SCROLL);
        LEVEL_TO_SCROLL.put(3, JOURNEYMAN_SCROLL);
        LEVEL_TO_SCROLL.put(4, EXPERT_SCROLL);
        LEVEL_TO_SCROLL.put(5, MASTER_SCROLL);
    }
}
