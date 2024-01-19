package svenhjol.strange.feature.quests.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.quests.QuestHelper;
import svenhjol.strange.feature.quests.QuestResources;
import svenhjol.strange.feature.quests.QuestsNetwork;
import svenhjol.strange.feature.quests.quest.GatherQuest;
import svenhjol.strange.feature.travel_journal.TravelJournalResources;
import svenhjol.strange.feature.travel_journal.client.Buttons;
import svenhjol.strange.feature.travel_journal.client.QuestScreen;

public class GatherQuestRenderer extends BaseQuestRenderer<GatherQuest> {
    boolean renderedButtons = false;
    int midX;
    int requirementColor;
    int satisfiedColor;
    int rewardColor;
    int requirementBackgroundColor;
    int satisfiedBackgroundColor;
    int rewardBackgroundColor;

    public GatherQuestRenderer() {}

    @Override
    public void initPagedOffered(Screen screen, int yOffset) {
        init(screen);
    }

    @Override
    public void initPagedActive(Screen screen, int yOffset) {
        init(screen);
    }

    @Override
    public void initSelectedActive(Screen screen) {
        init(screen);
    }

    /**
     * Shared init for all custom init calls.
     */
    protected void init(Screen screen) {
        midX = screen.width / 2;
        requirementColor = 0x303030;
        satisfiedColor = 0x308030;
        rewardColor = 0x308080;
        requirementBackgroundColor = 0x24000000;
        satisfiedBackgroundColor = 0x24008000;
        rewardBackgroundColor = 0x24008080;
        renderedButtons = false;
    }

    @Override
    public int getPagedOfferedHeight() {
        return super.getPagedOfferedHeight();
    }

    @Override
    public int getPagedActiveHeight() {
        return super.getPagedOfferedHeight();
    }

    /**
     * Render for each quest on an offered view (e.g. QuestOffersScreen).
     */
    @Override
    public void renderPagedOffered(Screen screen, GuiGraphics guiGraphics, int yOffset, int mouseX, int mouseY) {
        var midX = screen.width / 2;
        var font = screen.font;
        var xOffset = -155;
        var titleColor = 0xf0f0f0;
        var requirementColor = 0xc0c0c0;
        var rewardColor = 0xa0ffa0;

        var player = Minecraft.getInstance().player;
        if (player == null) return;

        // Scroll icon and title
        guiGraphics.blitSprite(QuestResources.LEVEL_TO_SCROLL.get(quest.villagerLevel()), midX + xOffset - 5, yOffset - 5, 16, 16);
        guiGraphics.drawString(font, QuestHelper.makeTitle(quest), midX + xOffset + 15, yOffset, titleColor);

        // Box around the quest details
        guiGraphics.renderOutline(midX + xOffset - 5, yOffset + 13, 320, 46, 0x40ffffff);

        // "Requires:"
        guiGraphics.drawString(font, QuestResources.GATHER_REQUIREMENT_TEXT, midX + xOffset + 2, yOffset + 21, requirementColor);

        // "Rewards:"
        guiGraphics.drawString(font, QuestResources.GATHER_REWARD_TEXT, midX + xOffset + 2, yOffset + 41, rewardColor);

        var xt = Math.max(font.width(QuestResources.GATHER_REQUIREMENT_TEXT), font.width(QuestResources.GATHER_REWARD_TEXT));

        var xo = midX + xOffset + xt + 14;
        var yo = yOffset + 18;
        for (var requirement : quest.requirements()) {
            if (requirement instanceof GatherQuest.GatherItem i) {
                var label = TextHelper.translatable("gui.strange.quests.gather_amount", i.total());
                guiGraphics.drawString(font, label, xo + 4, yo + 4, requirementColor);
                var width = font.width(label);

                guiGraphics.fill(xo, yo - 1, xo + 25 + width, yo + 17, 0x18ffffff);
                guiGraphics.renderFakeItem(i.item, xo + 7 + width, yo);

                if (mouseX >= xo && mouseX <= xo + 25 + width
                    && mouseY >= yo - 1 && mouseY <= yo + 17) {
                    guiGraphics.renderTooltip(font, i.item, xo, yo + 27);
                }
                xo += 27 + width;
            }
        }

        xo = midX + xOffset + xt + 14;
        yo = yOffset + 38;

        for (var reward : quest.rewards()) {
            if (reward instanceof GatherQuest.RewardItem i) {
                var label = TextHelper.translatable("gui.strange.quests.gather_amount", i.item.getCount());
                guiGraphics.drawString(font, label, xo + 4, yo + 4, rewardColor);
                var width = font.width(label);

                guiGraphics.fill(xo, yo - 1, xo + 25 + width, yo + 17, 0x1800ff00);
                guiGraphics.renderFakeItem(i.item, xo + 7 + width, yo);

                if (mouseX >= xo && mouseX <= xo + 25 + width
                    && mouseY >= yo - 1 && mouseY <= yo + 17) {
                    guiGraphics.renderTooltip(font, i.item, xo, yo + 27);
                }
                xo += 27 + width;
            }
        }

        if (!renderedButtons) {
            var button = new QuestOffersScreen.AcceptQuestButton(midX + 83, yOffset + 26, b -> {
                QuestsNetwork.AcceptQuest.send(quest.villagerUuid(), quest.id());
                screen.onClose();
            });

            screen.addRenderableWidget(button);
            renderedButtons = true;
        }
    }

    /**
     * Render for each quest on a paged view (e.g. QuestsScreen).
     */
    @Override
    public void renderPagedActive(Screen screen, GuiGraphics guiGraphics, int yOffset, int mouseX, int mouseY) {
        guiGraphics.drawString(screen.font, QuestHelper.makeTitle(quest), midX - 105, yOffset, 0x202020, false);
        renderRequirements(screen, guiGraphics, yOffset + 12, mouseX, mouseY);

        if (!renderedButtons) {
            var sprites = TravelJournalResources.LEVEL_TO_SCROLL_BUTTON.get(quest.villagerLevel());
            var button = new Buttons.ScrollImageButton(sprites, midX + 88, yOffset + 11, b -> {
                Minecraft.getInstance().setScreen(new QuestScreen(quest));
            }, TravelJournalResources.UPDATE_QUEST);
            screen.addRenderableWidget(button);
            renderedButtons = true;
        }
    }

    /**
     * Render for a selected quest (e.g. QuestScreen).
     */
    @Override
    public void renderSelectedActive(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(screen.font, QuestResources.GATHER_REQUIREMENT_TEXT, midX - 105, 40, 0x202020, false);
        renderRequirements(screen, guiGraphics, 52, mouseX, mouseY);

        guiGraphics.drawString(screen.font, QuestResources.GATHER_REWARD_TEXT, midX - 105, 80, 0x202020, false);
        renderRewards(screen, guiGraphics, 92, mouseX, mouseY);
    }

    /**
     * Shared requirements renderer for paged and selected.
     */
    protected void renderRequirements(Screen screen, GuiGraphics guiGraphics, int yOffset, int mouseX, int mouseY) {
        var font = screen.font;
        var xOffset = -105;

        var xo = midX + xOffset;
        var yo = yOffset;
        for (var requirement : quest.requirements()) {
            if (requirement instanceof GatherQuest.GatherItem i) {
                var label = TextHelper.translatable("gui.strange.quests.gather_remaining", Math.max(0, i.total() - i.remaining()), i.total());

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
                guiGraphics.renderTooltip(font, TextHelper.translatable("gui.strange.quests.satisfied"), xo, yo + 20);
            }
        }
    }

    protected void renderRewards(Screen screen, GuiGraphics guiGraphics, int yOffset, int mouseX, int mouseY) {
        var font = screen.font;
        var xOffset = -105;

        var xo = midX + xOffset;
        var yo = yOffset;
        for (var reward : quest.rewards()) {
            if (reward instanceof GatherQuest.RewardItem i) {
                var label = TextHelper.translatable("gui.strange.quests.gather_amount", i.item.getCount());

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
        }
    }
}
