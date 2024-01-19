package svenhjol.strange.feature.quests;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.Strange;

import java.util.HashMap;
import java.util.Map;

public class QuestResources {
    public static final Component QUEST_BUTTON_TEXT = TextHelper.translatable("gui.strange.quests.quests");
    public static final Component GATHER_REQUIREMENT_TEXT = TextHelper.translatable("gui.strange.quests.gather_requirement");
    public static final Component GATHER_REWARD_TEXT = TextHelper.translatable("gui.strange.quests.gather_reward");
    public static final Component ACCEPT_QUEST_BUTTON_TEXT = TextHelper.translatable("gui.strange.quests.accept_quest");
    public static final ResourceLocation VILLAGER_OFFERS_BACKGROUND = new ResourceLocation(Strange.ID, "textures/gui/villager_offers.png");
    public static final ResourceLocation NOVICE_SCROLL = new ResourceLocation(Strange.ID, "scroll/novice_scroll");
    public static final ResourceLocation APPRENTICE_SCROLL = new ResourceLocation(Strange.ID, "scroll/apprentice_scroll");
    public static final ResourceLocation JOURNEYMAN_SCROLL = new ResourceLocation(Strange.ID, "scroll/journeyman_scroll");
    public static final ResourceLocation EXPERT_SCROLL = new ResourceLocation(Strange.ID, "scroll/expert_scroll");
    public static final ResourceLocation MASTER_SCROLL = new ResourceLocation(Strange.ID, "scroll/master_scroll");
    public static final ResourceLocation TICK = new ResourceLocation(Strange.ID, "quest/tick");
    public static final String QUEST_OFFERS_TITLE_KEY = "gui.strange.quests.quest_offers";
    public static final String QUEST_TITLE_KEY = "gui.strange.quests.quest_title";
    public static final Map<Integer, ResourceLocation> LEVEL_TO_SCROLL = new HashMap<>();
    public static final Pair<Integer, Integer> VILLAGER_OFFERS_BACKGROUND_DIM = Pair.of(512, 256);

    static {
        LEVEL_TO_SCROLL.put(1, NOVICE_SCROLL);
        LEVEL_TO_SCROLL.put(2, APPRENTICE_SCROLL);
        LEVEL_TO_SCROLL.put(3, JOURNEYMAN_SCROLL);
        LEVEL_TO_SCROLL.put(4, EXPERT_SCROLL);
        LEVEL_TO_SCROLL.put(5, MASTER_SCROLL);
    }
}