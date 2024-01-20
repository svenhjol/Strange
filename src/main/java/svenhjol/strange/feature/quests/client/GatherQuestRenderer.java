package svenhjol.strange.feature.quests.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.quests.QuestHelper;
import svenhjol.strange.feature.quests.client.QuestButtons.AbandonButton;
import svenhjol.strange.feature.quests.client.QuestButtons.AcceptButton;
import svenhjol.strange.feature.quests.client.QuestButtons.ScrollImageButton;
import svenhjol.strange.feature.quests.quest.GatherQuest;

public class GatherQuestRenderer extends BaseQuestRenderer<GatherQuest> {
    boolean renderedButtons = false;

    public GatherQuestRenderer() {}

    @Override
    protected void init(Screen screen) {
        super.init(screen);
        renderedButtons = false;
    }

    @Override
    public int getPagedOfferHeight() {
        return super.getPagedOfferHeight();
    }

    @Override
    public int getPagedActiveHeight() {
        return super.getPagedActiveHeight();
    }

    /**
     * Render for each quest on an offered view (e.g. QuestOffersScreen).
     */
    @Override
    public void renderPagedOffer(Screen screen, GuiGraphics guiGraphics, int yOffset, int mouseX, int mouseY) {
        var midX = screen.width / 2;
        var font = screen.font;
        var xOffset = -155;

        var player = Minecraft.getInstance().player;
        if (player == null) return;

        // Scroll icon and title
        guiGraphics.blitSprite(QuestResources.LEVEL_TO_SCROLL.get(quest.villagerLevel()), midX + xOffset - 5, yOffset - 5, 16, 16);
        guiGraphics.drawString(font, QuestHelper.makeQuestTitle(quest), midX + xOffset + 15, yOffset, titleColor, false);

        // Box around the quest details
        guiGraphics.renderOutline(midX + xOffset - 5, yOffset + 13, 320, 46, offerBorderColor);

        // "Requires:"
        guiGraphics.drawString(font, QuestResources.GATHER_REQUIREMENT_TEXT, midX + xOffset + 2, yOffset + 21, requirementColor, false);

        // "Rewards:"
        guiGraphics.drawString(font, QuestResources.GATHER_REWARD_TEXT, midX + xOffset + 2, yOffset + 41, rewardColor, false);

        // Calculate the width of the reward/requirement text and start rendering images from there.
        var xt = Math.max(font.width(QuestResources.GATHER_REQUIREMENT_TEXT), font.width(QuestResources.GATHER_REWARD_TEXT));

        renderRequirements(screen, guiGraphics, -155 + xt + 14, yOffset + 18, mouseX, mouseY);
        renderRewards(screen, guiGraphics, -155 + xt + 14, yOffset + 38, mouseX, mouseY);

        if (!renderedButtons) {
            var button = new AcceptButton(midX + 83, yOffset + 26, onAccept);
            screen.addRenderableWidget(button);
            renderedButtons = true;
        }
    }

    /**
     * Render for each quest on a paged view (e.g. QuestsScreen).
     */
    @Override
    public void renderPagedActive(Screen screen, GuiGraphics guiGraphics, int yOffset, int mouseX, int mouseY) {
        guiGraphics.drawString(screen.font, QuestHelper.makeQuestTitle(quest), midX - 105, yOffset, titleColor, false);
        renderRequirements(screen, guiGraphics, -105, yOffset + 12, mouseX, mouseY);

        if (!renderedButtons) {
            var sprites = QuestResources.LEVEL_TO_SCROLL_BUTTON.get(quest.villagerLevel());
            var button = new ScrollImageButton(sprites, midX + 88, yOffset + 11, onUpdate, QuestResources.QUEST_INFO_TEXT);
            screen.addRenderableWidget(button);
            renderedButtons = true;
        }
    }

    /**
     * Render for a selected quest (e.g. QuestScreen).
     */
    @Override
    public void renderSelectedActive(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(screen.font, QuestResources.GATHER_REQUIREMENT_TEXT, midX - 105, 40, titleColor, false);
        renderRequirements(screen, guiGraphics, -105, 52, mouseX, mouseY);

        guiGraphics.drawString(screen.font, QuestResources.GATHER_REWARD_TEXT, midX - 105, 80, titleColor, false);
        renderRewards(screen, guiGraphics, -105, 92, mouseX, mouseY);

        if (!renderedButtons) {
            var button = new AbandonButton(midX - (AbandonButton.WIDTH / 2), 170, onAbandon);
            screen.addRenderableWidget(button);
            renderedButtons = true;
        }
    }

    /**
     * Shared requirements renderer for paged and selected.
     */
    protected void renderRequirements(Screen screen, GuiGraphics guiGraphics, int xOffset, int yOffset, int mouseX, int mouseY) {
        var font = screen.font;

        var xo = midX + xOffset;
        var yo = yOffset;
        for (var requirement : quest.requirements()) {
            if (requirement instanceof GatherQuest.GatherItem i) {
                var label = TextHelper.translatable(remainingKey, Math.max(0, i.total() - i.remaining()), i.total());

                guiGraphics.drawString(font, label, xo + 4, yo + 4, i.satisfied() ? satisfiedColor : requirementColor, false);
                var width = font.width(label);

                guiGraphics.fill(xo, yo - 1, xo + 25 + width, yo + 17, i.satisfied() ? satisfiedBackgroundColor : requirementBackgroundColor);
                guiGraphics.renderFakeItem(i.item, xo + 7 + width, yo);

                if (mouseX >= xo && mouseX <= xo + 25 + width
                    && mouseY >= yo - 1 && mouseY <= yo + 17) {
                    guiGraphics.renderTooltip(font, i.item, xo, yo + 27);
                }
                xo += 27 + width;
            }
        }

        if (quest.satisfied()) {
            guiGraphics.blitSprite(QuestResources.TICK, xo + 2, yo + 3, 9, 9);
            if (mouseX >= xo + 2 && mouseX <= xo + 11
                && mouseY >= yo + 3 && mouseY <= yo + 12) {
                guiGraphics.renderTooltip(font, TextHelper.translatable(QuestResources.SATISFIED_KEY), xo, yo + 20);
            }
        }
    }
}
