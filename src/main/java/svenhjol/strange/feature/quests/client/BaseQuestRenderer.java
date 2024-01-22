package svenhjol.strange.feature.quests.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.QuestHelper;
import svenhjol.strange.feature.quests.reward.RewardItem;
import svenhjol.strange.feature.quests.reward.RewardXp;

public abstract class BaseQuestRenderer<Q extends Quest> {
    Q quest;
    boolean renderedButtons = false;
    int midX;
    int titleColor;
    int requirementColor;
    int satisfiedColor;
    int rewardColor;
    int requirementBackgroundColor;
    int satisfiedBackgroundColor;
    int rewardBackgroundColor;
    int offerBorderColor;
    boolean showRemaining = false;
    Button.OnPress onAccept;
    Button.OnPress onUpdate;
    Button.OnPress onAbandon;
    Component questTitle;
    Component questTitleWithProfession;

    public BaseQuestRenderer() {}

    public void initPagedOffer(Screen screen, Button.OnPress onAccept, int yOffset) {
        init(screen);
        offerTheme();
        this.onAccept = onAccept;
    }

    public void initPagedActive(Screen screen, Button.OnPress onUpdate, int yOffset) {
        init(screen);
        activeTheme();
        this.onUpdate = onUpdate;
    }

    public void initSelectedActive(Screen screen, Button.OnPress onAbandon) {
        init(screen);
        activeTheme();
        this.onAbandon = onAbandon;
    }

    protected void offerTheme() {
        var isEpic = quest.isEpic();
        titleColor = isEpic ? 0xffe0a0 : 0xffffff;
        requirementColor = isEpic ? 0xdfc080 : 0xa0a0a0;
        satisfiedColor = 0xa0ffa0;
        rewardColor = 0xa0ffff;
        requirementBackgroundColor = isEpic ? 0x24dfc080 : 0x24a0a0a0;
        satisfiedBackgroundColor = 0x24a0ffa0;
        rewardBackgroundColor = 0x24a0ffff;
        offerBorderColor = isEpic ? 0x40ffe080 : 0x40a0a0a0;
        showRemaining = false;
    }

    protected void activeTheme() {
        titleColor = 0x202020;
        requirementColor = 0x303030;
        satisfiedColor = 0x308030;
        rewardColor = 0x308080;
        requirementBackgroundColor = 0x24000000;
        satisfiedBackgroundColor = 0x24008000;
        rewardBackgroundColor = 0x24008080;
        showRemaining = true;
    }

    protected void init(Screen screen) {
        midX = screen.width / 2;
        renderedButtons = false;
        questTitle = QuestHelper.makeQuestTitle(quest);
        questTitleWithProfession = QuestHelper.makeQuestTitleWithProfession(quest);
    }

    public Q quest() {
        return quest;
    }

    public void setQuest(Q quest) {
        this.quest = quest;
    }

    public int getPagedOfferHeight() {
        return 70;
    }

    public int getPagedActiveHeight() {
        return 40;
    }

    public abstract Component getRequirementText();

    public abstract Component getRewardText();

    public abstract ItemStack getQuestIcon();

    public void renderPagedActive(Screen screen, GuiGraphics guiGraphics, int yOffset, int mouseX, int mouseY) {
        guiGraphics.drawString(screen.font, questTitleWithProfession, midX - 105, yOffset, titleColor, false);
        renderRequirements(screen, guiGraphics, -105, yOffset + 12, mouseX, mouseY);

        if (!renderedButtons) {
            var sprites = QuestResources.LEVEL_TO_SCROLL_BUTTON.get(quest.villagerLevel());
            var button = new QuestButtons.ScrollImageButton(sprites, midX + 88, yOffset + 11, onUpdate, QuestResources.QUEST_INFO_TEXT);
            screen.addRenderableWidget(button);
            renderedButtons = true;
        }
    }

    public void renderSelectedActive(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(screen.font, getRequirementText(), midX - 105, 40, titleColor, false);
        renderRequirements(screen, guiGraphics, -105, 52, mouseX, mouseY);

        guiGraphics.drawString(screen.font, getRewardText(), midX - 105, 80, titleColor, false);
        renderRewards(screen, guiGraphics, -105, 92, mouseX, mouseY);

        if (!renderedButtons) {
            var button = new QuestButtons.AbandonButton(midX - (QuestButtons.AbandonButton.WIDTH / 2), 170, onAbandon);
            screen.addRenderableWidget(button);
            renderedButtons = true;
        }
    }

    /**
     * Render for each quest on an offered view (e.g. QuestOffersScreen).
     */
    public void renderPagedOffer(Screen screen, GuiGraphics guiGraphics, int yOffset, int mouseX, int mouseY) {
        var midX = screen.width / 2;
        var font = screen.font;
        var xOffset = -155;

        var player = Minecraft.getInstance().player;
        if (player == null) return;

        // Quest icon and title
        guiGraphics.renderFakeItem(getQuestIcon(), midX + xOffset - 5, yOffset - 5);
        guiGraphics.drawString(font, questTitle, midX + xOffset + 15, yOffset, titleColor, false);

        // Box around the quest details
        guiGraphics.renderOutline(midX + xOffset - 5, yOffset + 13, 320, 46, offerBorderColor);

        // "Requires:"
        guiGraphics.drawString(font, getRequirementText(), midX + xOffset + 2, yOffset + 21, requirementColor, false);

        // "Rewards:"
        guiGraphics.drawString(font, getRewardText(), midX + xOffset + 2, yOffset + 41, rewardColor, false);

        // Calculate the width of the reward/requirement text and start rendering images from there.
        var xt = Math.max(font.width(getRequirementText()), font.width(getRewardText()));

        renderRequirements(screen, guiGraphics, -155 + xt + 14, yOffset + 18, mouseX, mouseY);
        renderRewards(screen, guiGraphics, -155 + xt + 14, yOffset + 38, mouseX, mouseY);

        if (!renderedButtons) {
            var button = new QuestButtons.AcceptButton(midX + 83, yOffset + 26, onAccept);
            screen.addRenderableWidget(button);
            renderedButtons = true;
        }
    }

    protected abstract void renderRequirements(Screen screen, GuiGraphics guiGraphics, int xOffset, int yOffset, int mouseX, int mouseY);

    /**
     * Shared rewards renderer.
     */
    protected void renderRewards(Screen screen, GuiGraphics guiGraphics, int xOffset, int yOffset, int mouseX, int mouseY) {
        var font = screen.font;

        var xo = midX + xOffset;
        var yo = yOffset;
        for (var reward : quest.rewards()) {
            if (reward instanceof RewardItem i) {
                var label = TextHelper.translatable(QuestResources.AMOUNT_KEY, i.item.getCount());

                guiGraphics.drawString(font, label, xo + 4, yo + 4, rewardColor, false);
                var width = font.width(label);

                guiGraphics.fill(xo, yo - 1, xo + 25 + width, yo + 17, rewardBackgroundColor);
                guiGraphics.renderFakeItem(i.item, xo + 7 + width, yo);

                if (mouseX >= xo && mouseX <= xo + 25 + width
                    && mouseY >= yo - 1 && mouseY <= yo + 17) {
                    guiGraphics.renderTooltip(font, i.item, xo, yo + 27);
                }
                xo += 27 + width;
            }
            if (reward instanceof RewardXp i) {
                var label = TextHelper.translatable(QuestResources.AMOUNT_KEY, i.total);

                guiGraphics.drawString(font, label, xo + 4, yo + 4, rewardColor, false);
                var width = font.width(label);

                guiGraphics.fill(xo, yo - 1, xo + 25 + width, yo + 17, rewardBackgroundColor);
                guiGraphics.renderFakeItem(new ItemStack(Items.EXPERIENCE_BOTTLE), xo + 7 + width, yo);

                if (mouseX >= xo && mouseX <= xo + 25 + width
                    && mouseY >= yo - 1 && mouseY <= yo + 17) {
                    guiGraphics.renderTooltip(font, TextHelper.translatable(QuestResources.REWARD_LEVELS_KEY, i.total), xo, yo + 27);
                }
                xo += 27 + width;
            }
        }
    }
}
