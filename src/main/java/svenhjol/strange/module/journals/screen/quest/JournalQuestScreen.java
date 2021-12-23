package svenhjol.strange.module.journals.screen.quest;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import svenhjol.strange.Strange;
import svenhjol.strange.helper.GuiHelper;
import svenhjol.strange.module.journals.JournalsClient;
import svenhjol.strange.module.journals.helper.JournalHelper;
import svenhjol.strange.module.knowledge.Learnable;
import svenhjol.strange.module.knowledge_stones.KnowledgeStones;
import svenhjol.strange.module.quests.*;
import svenhjol.strange.module.quests.component.GatherComponent;
import svenhjol.strange.module.quests.component.HuntComponent;
import svenhjol.strange.module.quests.component.RewardComponent;
import svenhjol.strange.module.quests.definition.QuestDefinition;
import svenhjol.strange.module.runestones.RunestoneMaterial;
import svenhjol.strange.module.runestones.Runestones;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")
public class JournalQuestScreen extends JournalBaseQuestScreen {
    private static final Component RUNE_LABEL;
    private static ItemStack RUNE_ICON;

    private Map<ItemStack, Integer> items;
    private int playerXp;
    private int merchantXp;
    private QuestDefinition definition;
    private Component description;
    private Component hint;
    private GatherComponent gather;
    private HuntComponent hunt;

    private boolean showRuneReward;
    private final int rowHeight;

    public JournalQuestScreen(Quest quest) {
        super(quest);

        rowHeight = 15;
        showRuneReward = Strange.LOADER.isEnabled(Quests.class) && Quests.rewardRunes;

        if (Strange.LOADER.isEnabled("knowledge_stones")) {
            RUNE_ICON = new ItemStack(KnowledgeStones.KNOWLEDGE_STONES.get(Learnable.RUNE));
        } else {
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

        RewardComponent reward = quest.getComponent(RewardComponent.class);
        items = reward.getItems();
        playerXp = reward.getPlayerXp();
        merchantXp = reward.getMerchantXp();

        bottomButtons.add(0, new GuiHelper.ButtonDefinition(b -> quests(), GO_BACK));
        bottomButtons.add(1, new GuiHelper.ButtonDefinition(b -> objectives(), OBJECTIVES));

        bottomNavButtons.add(new GuiHelper.ImageButtonDefinition(b -> abandon(), NAVIGATION, 20, 0, 18, ABANDON_TOOLTIP));
        rightNavButtons.add(new GuiHelper.ImageButtonDefinition(b -> pause(), NAVIGATION, 100, 36, 18, PAUSE_TOOLTIP));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        super.render(poseStack, mouseX, mouseY, delta);
        int top = 34;
        int left = midX - 64;

        if (quest.isSatisfied(minecraft.player)) {
            top = GuiHelper.drawWordWrap(font, QUEST_SATISFIED, left - 40, top, 220, textColor);
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
        GuiHelper.drawCenteredString(poseStack, font, OBJECTIVES, midX, top, subheadingColor);
        top += 12;

        if (!gather.getItems().isEmpty()) {
            List<Component> hover = new ArrayList<>();
            int totalRequired = 0;
            int totalGathered = 0;

            for (Map.Entry<ItemStack, Integer> entry : gather.getItems().entrySet()) {
                ItemStack item = entry.getKey();
                Integer requiredCount = entry.getValue();
                Integer gatheredCount = gather.getSatisfied().getOrDefault(item, 0);
                hover.add(new TranslatableComponent("gui.strange.journal.hover_completion", item.getHoverName(), gatheredCount, requiredCount));

                totalGathered += gatheredCount;
                totalRequired += requiredCount;
            }

            Component label = new TranslatableComponent("gui.strange.journal.items_gathered", totalGathered, totalRequired);
            renderComponentIcon(poseStack, gather, GATHER_ICON, COMPLETED_GATHER_ICON, left, top);
            renderCompletionTextAndHover(poseStack, gather, label, hover, left, top, mouseX, mouseY);
            top += rowHeight;
        }

        if (!hunt.getEntities().isEmpty()) {
            List<Component> hover = new ArrayList<>();
            int totalRequired = 0;
            int totalKilled = 0;

            for (Map.Entry<ResourceLocation, Integer> entry : hunt.getEntities().entrySet()) {
                ResourceLocation entityId = entry.getKey();
                EntityType<?> entity = Registry.ENTITY_TYPE.get(entityId);
                int requiredCount = entry.getValue();
                int killedCount = hunt.getKilled().getOrDefault(entityId, 0);
                hover.add(new TranslatableComponent("gui.strange.journal.hover_completion", entity.getDescription().getString(), killedCount, requiredCount));

                totalKilled += killedCount;
                totalRequired += requiredCount;
            }

            Component label = new TranslatableComponent("gui.strange.journal.mobs_hunted", totalKilled, totalRequired);
            renderComponentIcon(poseStack, hunt, HUNT_ICON, COMPLETED_HUNT_ICON, left, top);
            renderCompletionTextAndHover(poseStack, hunt, label, hover, left, top, mouseX, mouseY);
            top += rowHeight;
        }

        return top + 8;
    }

    private int renderRewards(PoseStack poseStack, int top, int left, int mouseX, int mouseY) {
        GuiHelper.drawCenteredString(poseStack, font, REWARDS, midX, top, subheadingColor);
        top += 12;

        if (items.size() == 0 && playerXp == 0 && merchantXp == 0) {
            GuiHelper.drawCenteredString(poseStack, font, NO_REWARDS, midX, top, secondaryColor);
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
                itemRenderer.renderGuiItem(EXPERIENCE_ICON, left, top - 5);
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
