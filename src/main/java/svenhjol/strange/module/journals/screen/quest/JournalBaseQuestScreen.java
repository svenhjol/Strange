package svenhjol.strange.module.journals.screen.quest;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.screen.JournalScreen;
import svenhjol.strange.module.quests.Quest;
import svenhjol.strange.module.quests.QuestsClient;

public abstract class JournalBaseQuestScreen extends JournalScreen {
    protected final Quest quest;

    protected static final Component REWARDS;
    protected static final Component NO_REWARDS;
    protected static final Component OBJECTIVES;
    protected static final Component QUEST_SATISFIED;
    protected static final Component ABANDON_TOOLTIP;
    protected static final Component PAUSE_TOOLTIP;
    protected static final ItemStack EXPERIENCE_ICON;
    protected static final ItemStack GATHER_ICON;
    protected static final ItemStack HUNT_ICON;
    protected static final ItemStack EXPLORE_ICON;
    protected static final ItemStack BOSS_ICON;
    protected static final ItemStack COMPLETED_GATHER_ICON;
    protected static final ItemStack COMPLETED_HUNT_ICON;
    protected static final ItemStack COMPLETED_EXPLORE_ICON;
    protected static final ItemStack COMPLETED_BOSS_ICON;

    public JournalBaseQuestScreen(Quest quest) {
        super(new TextComponent(QuestsClient.getTitle(quest.getDefinition())));
        this.quest = quest;

        COMPLETED_GATHER_ICON.enchant(Enchantments.UNBREAKING, 1);
        COMPLETED_HUNT_ICON.enchant(Enchantments.UNBREAKING, 1);
        COMPLETED_EXPLORE_ICON.enchant(Enchantments.UNBREAKING, 1);
        COMPLETED_BOSS_ICON.enchant(Enchantments.UNBREAKING, 1);
    }

    @Override
    protected void init() {
        super.init();

        JournalsClient.tracker.setQuest(quest);
    }

    static {
        REWARDS = new TranslatableComponent("gui.strange.journal.rewards");
        NO_REWARDS = new TranslatableComponent("gui.strange.journal.no_rewards");
        OBJECTIVES = new TranslatableComponent("gui.strange.journal.objectives");
        QUEST_SATISFIED = new TranslatableComponent("gui.strange.journal.quest_satisfied");
        ABANDON_TOOLTIP = new TranslatableComponent("gui.strange.journal.abandon_tooltip");
        PAUSE_TOOLTIP = new TranslatableComponent("gui.strange.journal.pause_tooltip");
        EXPERIENCE_ICON = new ItemStack(Items.EXPERIENCE_BOTTLE);
        GATHER_ICON = new ItemStack(Items.BUNDLE);
        HUNT_ICON = new ItemStack(Items.STONE_SWORD);
        EXPLORE_ICON = new ItemStack(Items.CHEST);
        BOSS_ICON = new ItemStack(Items.WITHER_SKELETON_SKULL);
        COMPLETED_GATHER_ICON = new ItemStack(Items.BUNDLE);
        COMPLETED_HUNT_ICON = new ItemStack(Items.STONE_SWORD);
        COMPLETED_EXPLORE_ICON = new ItemStack(Items.CHEST);
        COMPLETED_BOSS_ICON = new ItemStack(Items.WITHER_SKELETON_SKULL);
    }
}
