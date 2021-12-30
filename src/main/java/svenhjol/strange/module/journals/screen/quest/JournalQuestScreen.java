package svenhjol.strange.module.journals.screen.quest;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.Strange;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.helper.JournalHelper;
import svenhjol.strange.module.journals.screen.JournalResources;
import svenhjol.strange.module.journals.screen.JournalScreen;
import svenhjol.strange.module.quests.*;
import svenhjol.strange.module.quests.component.*;
import svenhjol.strange.module.quests.definition.QuestDefinition;
import svenhjol.strange.module.runestones.RunestoneMaterial;
import svenhjol.strange.module.runestones.Runestones;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")
public class JournalQuestScreen extends JournalScreen {
    private static final Component RUNE_LABEL;
    private static ItemStack RUNE_ICON;

    private final Quest quest;
    private Map<ItemStack, Integer> items;
    private int playerXp;
    private int merchantXp;
    private QuestDefinition definition;
    private Component description;
    private Component hint;
    private GatherComponent gather;
    private HuntComponent hunt;
    private ExploreComponent explore;
    private BossComponent boss;

    private boolean showRuneReward;
    private final int rowHeight;

    public JournalQuestScreen(Quest quest) {
        super(new TextComponent(QuestsClient.getTitle(quest.getDefinition())));
        this.quest = quest;

        rowHeight = 15;
        showRuneReward = Strange.LOADER.isEnabled(Quests.class) && Quests.rewardRunes;

        if (Strange.LOADER.isEnabled("knowledge_stones")) {
            RUNE_ICON = new ItemStack(Runestones.RUNESTONE_BLOCKS.get(RunestoneMaterial.STONE));
        }
    }

    @Override
    protected void init() {
        super.init();

        quest.update(minecraft.player);

        definition = quest.getDefinition();
        description = new TextComponent(QuestsClient.getDescription(definition));
        hint = new TextComponent(QuestsClient.getHint(definition));
        gather = quest.getComponent(GatherComponent.class);
        hunt = quest.getComponent(HuntComponent.class);
        explore = quest.getComponent(ExploreComponent.class);
        boss = quest.getComponent(BossComponent.class);

        RewardComponent reward = quest.getComponent(RewardComponent.class);
        items = reward.getItems();
        playerXp = reward.getPlayerXp();
        merchantXp = reward.getMerchantXp();

        bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> quests(), JournalResources.GO_BACK));

        // In future we could have an objectives breakdown page, but it's not needed for initial release.
        // bottomButtons.add(1, new GuiHelper.ButtonDefinition(b -> objectives(), OBJECTIVES));

        bottomNavButtons.add(new GuiHelper.ImageButtonDefinition(b -> abandon(), JournalResources.NAVIGATION, 20, 0, 18, JournalResources.ABANDON_TOOLTIP));
        rightNavButtons.add(new GuiHelper.ImageButtonDefinition(b -> pause(), JournalResources.NAVIGATION, 100, 36, 18, JournalResources.PAUSE_TOOLTIP));

        JournalsClient.tracker.setQuest(quest);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        int top = 34;
        int left = midX - 64;

        if (quest.isSatisfied(minecraft.player)) {
            top = GuiHelper.drawWordWrap(font, JournalResources.QUEST_SATISFIED, left - 40, top, 220, textColor);
        } else {
            if (description.getString().length() > 50) {
                top = GuiHelper.drawWordWrap(font, description, left - 40, top, 220, textColor);
            } else {
                GuiHelper.drawCenteredString(poseStack, font, description, midX, top, textColor);
                top += 9;
            }
        }
        top += 10;
        top = renderCompletion(poseStack, top, left, mouseX, mouseY);
        renderRewards(poseStack, top, left, mouseX, mouseY);
    }

    private int renderCompletion(PoseStack poseStack, int top, int left, int mouseX, int mouseY) {
        GuiHelper.drawCenteredString(poseStack, font, JournalResources.OBJECTIVES, midX, top, subheadingColor);
        top += 12;

        if (gather.isPresent()) {
            List<Component> hover = new ArrayList<>();
            int totalRequired = 0;
            int totalGathered = 0;

            for (var entry : gather.getItems().entrySet()) {
                var item = entry.getKey();
                var requiredCount = entry.getValue();
                var gatheredCount = gather.getSatisfied().getOrDefault(item, 0);
                hover.add(new TranslatableComponent("gui.strange.journal.hover_completion", item.getHoverName(), gatheredCount, requiredCount));

                totalGathered += gatheredCount;
                totalRequired += requiredCount;
            }

            var label = new TranslatableComponent("gui.strange.journal.items_gathered", totalGathered, totalRequired);
            renderComponentIcon(poseStack, gather, JournalResources.GATHER_ICON, JournalResources.COMPLETED_GATHER_ICON, left, top);
            renderCompletionTextAndHover(poseStack, gather, label, hover, left, top, mouseX, mouseY);
            top += rowHeight;
        }

        if (hunt.isPresent()) {
            List<Component> hover = new ArrayList<>();
            var totalRequired = 0;
            var totalKilled = 0;

            for (var entry : hunt.getEntities().entrySet()) {
                var entityId = entry.getKey();
                var entity = Registry.ENTITY_TYPE.get(entityId);
                var description = entity.getDescription().getString();
                var requiredCount = entry.getValue();
                var killedCount = hunt.getKilled().getOrDefault(entityId, 0);
                hover.add(new TranslatableComponent("gui.strange.journal.hover_completion", description, killedCount, requiredCount));

                totalKilled += killedCount;
                totalRequired += requiredCount;
            }

            var label = new TranslatableComponent("gui.strange.journal.mobs_hunted", totalKilled, totalRequired);
            renderComponentIcon(poseStack, hunt, JournalResources.HUNT_ICON, JournalResources.COMPLETED_HUNT_ICON, left, top);
            renderCompletionTextAndHover(poseStack, hunt, label, hover, left, top, mouseX, mouseY);
            top += rowHeight;
        }

        if (explore.isPresent()) {
            List<Component> hover = new ArrayList<>();
            var totalRequired = 0;
            var totalGathered = 0;

            for (var stack : explore.getItems()) {
                var item = stack.getItem();
                var requiredCount = 1;
                var gatheredCount = explore.getSatisfied().getOrDefault(item, false) ? 1 : 0;
                hover.add(new TranslatableComponent("gui.strange.journal.hover_completion", stack.getHoverName(), gatheredCount, requiredCount));

                totalRequired += requiredCount;
                totalGathered += gatheredCount;
            }

            var label = new TranslatableComponent("gui.strange.journal.structures_explored", totalGathered, totalRequired);
            renderComponentIcon(poseStack, explore, JournalResources.EXPLORE_ICON, JournalResources.COMPLETED_EXPLORE_ICON, left, top);
            renderCompletionTextAndHover(poseStack, explore, label, hover, left, top, mouseX, mouseY);
            top += rowHeight;
        }

        if (boss.isPresent()) {
            List<Component> hover = new ArrayList<>();
            var totalRequired = 0;
            var totalKilled = 0;

            for (var entry : boss.getTargets().entrySet()) {
                var entryId = entry.getKey();
                var requiredCount = entry.getValue();
                var killedCount = boss.getKilled().getOrDefault(entryId, 0);
                var entity = Registry.ENTITY_TYPE.get(entryId);
                var description = entity.getDescription().getString();
                hover.add(new TranslatableComponent("gui.strange.journal.hover_completion", description, killedCount, requiredCount));

                totalRequired += requiredCount;
                totalKilled += killedCount;
            }

            var label = new TranslatableComponent("gui.strange.journal.bosses_defeated", totalKilled, totalRequired);
            renderComponentIcon(poseStack, boss, JournalResources.BOSS_ICON, JournalResources.COMPLETED_BOSS_ICON, left, top);
            renderCompletionTextAndHover(poseStack, boss, label, hover, left, top, mouseX, mouseY);
            top += rowHeight;
        }

        return top + 8;
    }

    private int renderRewards(PoseStack poseStack, int top, int left, int mouseX, int mouseY) {
        GuiHelper.drawCenteredString(poseStack, font, JournalResources.REWARDS, midX, top, subheadingColor);
        top += 12;

        if (items.size() == 0 && playerXp == 0 && merchantXp == 0) {
            GuiHelper.drawCenteredString(poseStack, font, JournalResources.NO_REWARDS, midX, top, secondaryColor);
        } else {
            Component playerXpLabel = new TranslatableComponent("gui.strange.journal.reward_levels", playerXp);

            // item render
            if (items.size() > 0) {
                int originalTop = top;
                List<ItemStack> stacks = new ArrayList<>(items.keySet());

                for (ItemStack stack : stacks) {
                    int count = items.get(stack);
                    Component label = new TranslatableComponent("gui.strange.journal.reward_item", stack.getHoverName(), count);
                    itemRenderer.renderGuiItem(stack, left, top - 5);
                    font.draw(poseStack, label, left + 19, top, textColor);
                    top += rowHeight;
                }

                // render item hover
                top = originalTop;
                for (ItemStack stack : stacks) {
                    if (mouseX > left && mouseX < left + 19 && mouseY > top - 5 && mouseY < top + 10) {
                        renderComponentTooltip(poseStack, getTooltipFromItem(stack), mouseX, mouseY);
                    }
                    top += rowHeight;
                }
            }

            // xp levels
            if (playerXp > 0) {
                itemRenderer.renderGuiItem(JournalResources.EXPERIENCE_ICON, left, top - 5);
                font.draw(poseStack, playerXpLabel, left + 19, top, textColor);
                top += 16;
            }
        }

        top = renderRuneReward(poseStack, top, left, mouseX, mouseY);
        return top + 8;
    }

    private int renderRuneReward(PoseStack poseStack, int top, int left, int mouseX, int mouseY) {
        if (!showRuneReward) return top;

        var journal = JournalsClient.getJournal().orElse(null);
        if (journal == null) return top;

        var tier = quest.getTier();
        var rune = JournalHelper.nextLearnableRune(tier, journal);
        if (rune < 0) {
            showRuneReward = false;
            return top;
        }

        itemRenderer.renderGuiItem(RUNE_ICON, left, top - 5);
        font.draw(poseStack, RUNE_LABEL, left + 19, top, textColor);

        if (mouseX > left && mouseX < left + 19 + (RUNE_LABEL.getString().length() * 6) && mouseY > top - 5 && mouseY < top + 10) {
            String s = I18n.get("gui.strange.journal.reward_rune_tooltip");
            List<Component> lines = Arrays.stream(s.split("\n")).map(TextComponent::new).collect(Collectors.toList());
            renderComponentTooltip(poseStack, lines, mouseX, mouseY);
        }

        return top;
    }

    private void renderCompletionTextAndHover(PoseStack poseStack, IQuestComponent component, Component label, List<Component> hover, int left, int top, int mouseX, int mouseY) {
        font.draw(poseStack, label, left + 19, top, component.isSatisfied(minecraft.player) ? completedColor : textColor);
        if (mouseX > left && mouseX < left + 19 + (label.getString().length() * 6) && mouseY > top - 5 && mouseY < top + 10) {
            renderComponentTooltip(poseStack, hover, mouseX, mouseY);
        }
    }

    private void renderComponentIcon(PoseStack poseStack, IQuestComponent component, ItemStack incomplete, ItemStack complete, int left, int top) {
        if (component.isSatisfied(minecraft.player)) {
            QuestsClient.renderIcon(this, poseStack, QuestIcons.ICON_TICK, left - 12, top);
            itemRenderer.renderGuiItem(complete, left, top - 5);
        } else {
            itemRenderer.renderGuiItem(incomplete, left, top - 5);
        }
    }

    protected void objectives() {

    }

    protected void abandon() {
        QuestsClient.CLIENT_SEND_ABANDON_QUEST.send(quest);
    }

    protected void pause() {
        QuestsClient.CLIENT_SEND_PAUSE_QUEST.send(quest);
    }

    static {
        RUNE_LABEL = new TranslatableComponent("gui.strange.journal.reward_rune");
    }
}
