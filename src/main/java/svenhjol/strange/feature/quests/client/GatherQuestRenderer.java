package svenhjol.strange.feature.quests.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.quests.QuestResources;
import svenhjol.strange.feature.quests.QuestsNetwork;
import svenhjol.strange.feature.quests.quest.GatherQuest;
import svenhjol.strange.feature.travel_journal.TravelJournalResources;
import svenhjol.strange.feature.travel_journal.client.BaseTravelJournalScreen;
import svenhjol.strange.feature.travel_journal.client.Buttons.ScrollImageButton;

public class GatherQuestRenderer extends BaseQuestRenderer<GatherQuest> {
    boolean renderedButtons = false;

    public GatherQuestRenderer() {}

    @Override
    public void initOffered(Screen screen, int yOffset) {
        renderedButtons = false;
    }

    @Override
    public void initPaged(BaseTravelJournalScreen screen, int yOffset) {
        renderedButtons = false;
    }

    @Override
    public int getOfferedHeight() {
        return super.getOfferedHeight();
    }

    @Override
    public int getPagedHeight() {
        return 40;
    }

    @Override
    public void renderOffered(Screen screen, GuiGraphics guiGraphics, int yOffset, int mouseX, int mouseY) {
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
        guiGraphics.drawString(font, makeTitle(), midX + xOffset + 15, yOffset, titleColor);

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
                guiGraphics.drawString(font, label, xo + 4, yo + 4, requirementColor);
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

    @Override
    public void renderPaged(BaseTravelJournalScreen screen, GuiGraphics guiGraphics, int yOffset, int mouseX, int mouseY) {
        var midX = screen.width / 2;
        var font = screen.font;
        var xOffset = -105;
        var titleColor = 0x202020;
        var requirementColor = 0x303030;
        var satisfiedColor = 0x308030;

        guiGraphics.drawString(font, makeTitle(), midX + xOffset, yOffset, titleColor, false);

        var xo = midX + xOffset;
        var yo = yOffset + 12;
        for (var requirement : quest.requirements()) {
            if (requirement instanceof GatherQuest.GatherItem i) {
                var label = TextHelper.translatable("gui.strange.quests.gather_remaining", Math.max(0, i.total() - i.remaining()), i.total());

                guiGraphics.drawString(font, label, xo + 4, yo + 4, i.satisfied() ? satisfiedColor : requirementColor, false);
                var width = font.width(label);

                guiGraphics.fill(xo, yo - 1, xo + 25 + width, yo + 17, i.satisfied() ? 0x24008800 : 0x24000000);
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

        if (!renderedButtons) {
            var sprites = TravelJournalResources.LEVEL_TO_SCROLL_BUTTON.get(quest.villagerLevel());
            var button = new ScrollImageButton(sprites, midX + 88, yOffset + 11, b -> {}, TravelJournalResources.UPDATE_QUEST);
            screen.addRenderableWidget(button);
            renderedButtons = true;
        }
    }

    protected Component makeTitle() {
        return TextHelper.translatable(QuestResources.QUEST_TITLE_KEY,
            TextHelper.translatable("merchant.level." + quest.villagerLevel()),
            TextHelper.translatable("gui.strange.quests.gathering"));
    }
}
