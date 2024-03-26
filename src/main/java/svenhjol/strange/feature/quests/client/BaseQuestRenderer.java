package svenhjol.strange.feature.quests.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import svenhjol.charmony.helper.ConfigHelper;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.QuestsHelper;
import svenhjol.strange.feature.quests.QuestsResources;
import svenhjol.strange.feature.quests.reward.RewardExperience;
import svenhjol.strange.feature.quests.reward.RewardItem;

public abstract class BaseQuestRenderer<Q extends Quest> {
    protected Q quest;
    protected ScreenType screenType;
    protected boolean renderedButtons = false;
    protected int midX;
    protected int titleColor;
    protected int requirementColor;
    protected int satisfiedColor;
    protected int rewardColor;
    protected int requirementBackgroundColor;
    protected int satisfiedBackgroundColor;
    protected int rewardBackgroundColor;
    protected int offerBorderColor;
    protected boolean showRemaining = false;
    protected Button.OnPress onAccept;
    protected Button.OnPress onUpdate;
    protected Button.OnPress onAbandon;
    protected Component questTitle;
    protected ItemStack questIcon;
    protected Screen screen;

    public BaseQuestRenderer() {}

    public void setAcceptAction(Button.OnPress onAccept) {
        this.onAccept = onAccept;
    }

    public void setUpdateAction(Button.OnPress onUpdate) {
        this.onUpdate = onUpdate;
    }

    public void setAbandonAction(Button.OnPress onAbandon) {
        this.onAbandon = onAbandon;
    }

    public void initPagedOffer(Screen screen) {
        init(ScreenType.PAGED_OFFER, screen);
        offerTheme();
        this.screen = screen;
    }

    public void initPagedActive(Screen screen) {
        init(ScreenType.PAGED_ACTIVE, screen);
        activeTheme();
        this.screen = screen;
    }

    public void initSelectedActive(Screen screen) {
        init(ScreenType.SELECTED_ACTIVE, screen);
        activeTheme();
        this.screen = screen;
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
        requirementBackgroundColor = 0x12000000;
        satisfiedBackgroundColor = 0x24008000;
        rewardBackgroundColor = 0x24008080;
        showRemaining = true;
    }

    protected void init(ScreenType screenType, Screen screen) {
        this.screenType = screenType;
        midX = screen.width / 2;
        renderedButtons = false;
        questTitle = QuestsHelper.makeQuestTitle(quest);
        questIcon = getQuestIcon();
    }

    public Q quest() {
        return quest;
    }

    public void setQuest(Q quest) {
        this.quest = quest;
    }

    public int getPagedOfferHeight() {
        return 68;
    }

    public int getPagedActiveHeight() {
        return 40;
    }

    public abstract Component getRequirementText();

    public abstract Component getRewardText();

    public abstract ItemStack getQuestIcon();

    public void renderPagedActive(GuiGraphics guiGraphics, int yOffset, int mouseX, int mouseY) {
        guiGraphics.drawString(screen.font, questTitle, midX - 105, yOffset, titleColor, false);
        renderRequirements(guiGraphics, -105, yOffset + 12, mouseX, mouseY);

        if (!renderedButtons) {
            var sprites = QuestsButtons.LEVEL_TO_SCROLL_BUTTON.get(quest.villagerLevel());
            var button = new QuestsButtons.ScrollImageButton(sprites, midX + 88, yOffset + 17, onUpdate, QuestsResources.QUEST_INFO_TEXT);
            screen.addRenderableWidget(button);
            renderedButtons = true;
        }
    }

    public void renderSelectedActive(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(screen.font, getRequirementText(), midX - 105, 40, titleColor, false);
        renderRequirements(guiGraphics, -105, 52, mouseX, mouseY);

        guiGraphics.drawString(screen.font, getRewardText(), midX - 105, 95, titleColor, false);
        renderRewards(guiGraphics, -105, 107, mouseX, mouseY);

        // Show the professions that this quest can be returned to.
        Component professionInfo;
        var professions = quest.villagerProfessions();

        if (professions.contains(VillagerProfession.NONE)) {
            // Any villager provides rewards.
            professionInfo = Component.translatable(QuestsResources.RETURN_TO_ANY_PROFESSION_KEY);
        } else if (professions.size() == 1) {
            // A specific villager profession provides rewards.
            var professionName = svenhjol.strange.helper.TextHelper.uncapitalize(QuestsHelper.getProfessionName(professions.get(0)).getString());
            professionInfo = Component.translatable(QuestsResources.RETURN_TO_PROFESSION_KEY, professionName);
        } else {
            // Any of the specified villager professions provide rewards.
            var sb = new StringBuilder();
            for (int i = 0; i < professions.size(); i++) {
                var profession = professions.get(i);
                var professionName = svenhjol.strange.helper.TextHelper.uncapitalize(QuestsHelper.getProfessionName(profession).getString());
                sb.append(professionName).append(i < professions.size() - 1 ? ", " : ".");
            }
            professionInfo = Component.translatable(QuestsResources.RETURN_TO_PROFESSIONS_KEY, sb.toString());
        }

        guiGraphics.drawWordWrap(screen.font, professionInfo, midX - 105, 128, 200, rewardColor);
    }

    /**
     * Render for each quest on an offered view (e.g. QuestOffersScreen).
     */
    public void renderPagedOffer(GuiGraphics guiGraphics, int yOffset, int mouseX, int mouseY) {
        var midX = screen.width / 2;
        var font = screen.font;
        var xOffset = -155;

        var player = Minecraft.getInstance().player;
        if (player == null) return;

        // Quest icon and title
        guiGraphics.renderFakeItem(questIcon, midX + xOffset - 5, yOffset - 4);
        guiGraphics.drawString(font, questTitle, midX + xOffset + 15, yOffset + 2, titleColor, false);

        // Show definition ID in a tooltip if debugging enabled
        if (ConfigHelper.isDebugEnabled()
            && mouseX > midX + xOffset - 5 && mouseX < midX + xOffset + 15
            && mouseY > yOffset + 2 && mouseY < yOffset + 14) {
            guiGraphics.renderTooltip(font, Component.literal(quest.definitionId().substring(quest.definitionId().lastIndexOf("/") + 1)),
                midX + xOffset - 5, yOffset + 24);
        }

        // Box around the quest details
        guiGraphics.renderOutline(midX + xOffset - 5, yOffset + 13, 320, 46, offerBorderColor);

        // "Requires:"
        guiGraphics.drawString(font, getRequirementText(), midX + xOffset + 1, yOffset + 22, requirementColor, false);

        // "Rewards:"
        guiGraphics.drawString(font, getRewardText(), midX + xOffset + 1, yOffset + 42, rewardColor, false);

        // Calculate the width of the reward/requirement text and start rendering images from there.
        var xt = Math.max(font.width(getRequirementText()), font.width(getRewardText()));

        renderRequirements(guiGraphics, -155 + xt + 6, yOffset + 18, mouseX, mouseY);
        renderRewards(guiGraphics, -155 + xt + 6, yOffset + 38, mouseX, mouseY);

        // Draw loyalty star and number of loyalty points required
        if (quest.loyalty() > 0) {
            var str = Component.literal(String.valueOf(quest.loyalty()));
            guiGraphics.drawString(font, str, (midX + 148) - font.width(str), yOffset + 3, titleColor, false);
            guiGraphics.blitSprite(QuestsResources.STAR, midX + 150, yOffset + 2, 9, 9);
        }

        if (!renderedButtons) {
            var button = new QuestsButtons.AcceptImageButton(midX + 133, yOffset + 26, onAccept);
            screen.addRenderableWidget(button);
            renderedButtons = true;
        }
    }

    protected abstract void renderRequirements(GuiGraphics guiGraphics, int xOffset, int yOffset, int mouseX, int mouseY);

    protected void renderRewards(GuiGraphics guiGraphics, int xOffset, int yOffset, int mouseX, int mouseY) {
        var font = screen.font;

        var xo = midX + xOffset;
        var yo = yOffset;

        for (var reward : quest.rewards()) {
            if (reward instanceof RewardItem i) {
                var label = TextHelper.translatable(QuestsResources.AMOUNT_KEY, i.stack.getCount());

                guiGraphics.drawString(font, label, xo + 3, yo + 5, rewardColor, false);
                var width = font.width(label);

                guiGraphics.fill(xo, yo - 1, xo + 22 + width, yo + 17, rewardBackgroundColor);
                guiGraphics.renderFakeItem(i.stack, xo + 5 + width, yo);

                if (mouseX >= xo + 5 + width && mouseX <= xo + 21 + width
                    && mouseY >= yo && mouseY <= yo + 16) {
                    guiGraphics.renderTooltip(font, i.stack, xo, yo + 27);
                }

                xo += 24 + width;
            }

            if (reward instanceof RewardExperience i) {
                var label = TextHelper.translatable(QuestsResources.AMOUNT_KEY, i.total);

                guiGraphics.drawString(font, label, xo + 3, yo + 5, rewardColor, false);
                var width = font.width(label);

                guiGraphics.fill(xo, yo - 1, xo + 21 + width, yo + 17, rewardBackgroundColor);
                guiGraphics.renderFakeItem(new ItemStack(Items.EXPERIENCE_BOTTLE), xo + 4 + width, yo);

                if (mouseX >= xo + 4 + width && mouseX <= xo + 20 + width
                    && mouseY >= yo && mouseY <= yo + 16) {
                    guiGraphics.renderTooltip(font, TextHelper.translatable(QuestsResources.REWARD_LEVELS_KEY, i.total), xo, yo + 27);
                }

                xo += 23 + width;
            }
        }
    }

    protected void renderSatisfied(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        var font = screen.font;

        if (quest.satisfied()) {
            guiGraphics.blitSprite(QuestsResources.TICK, x + 3, y + 3, 9, 9);
            if (mouseX >= x + 2 && mouseX <= x + 11
                && mouseY >= y + 3 && mouseY <= y + 12) {
                guiGraphics.renderTooltip(font, TextHelper.translatable(QuestsResources.SATISFIED_KEY), x, y + 20);
            }
        }
    }

    public enum ScreenType {
        PAGED_OFFER,
        PAGED_ACTIVE,
        SELECTED_ACTIVE;
    }
}
