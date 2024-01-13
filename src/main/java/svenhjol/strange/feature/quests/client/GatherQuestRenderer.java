package svenhjol.strange.feature.quests.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.quests.QuestResources;
import svenhjol.strange.feature.quests.quest.GatherQuest;

public class GatherQuestRenderer extends BaseQuestRenderer<GatherQuest> {
    boolean rendereredOfferButtons = false;

    public GatherQuestRenderer() {
    }

    @Override
    public int getOfferHeight() {
        return 65;
    }

    @Override
    public void initOffer(Screen screen, int yOffset) {
        rendereredOfferButtons = false;
    }

    @Override
    public void renderOffer(Screen screen, GuiGraphics guiGraphics, int yOffset, int mouseX, int mouseY) {
        var midX = screen.width / 2;
        var font = screen.font;
        var xOffset = -155;
        var requirementColor = 0xd0d0d0;
        var rewardColor = 0xa0ffa0;

        guiGraphics.renderOutline(midX + xOffset - 5, yOffset - 5, 320, 46, 0x40ffffff);

        // Title: "You provide:"
        guiGraphics.drawString(font, QuestResources.GATHER_REQUIREMENT_TEXT, midX + xOffset + 4, yOffset + 4, requirementColor);

        var xo = midX + xOffset + 80;
        var yo = yOffset;
        for (var requirement : quest.requirements()) {
            if (requirement instanceof GatherQuest.GatherItem i) {
                var label = TextHelper.translatable("gui.strange.quests.gather_amount", i.total);
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

        // Title: "You receive:"
        guiGraphics.drawString(font, QuestResources.GATHER_REWARD_TEXT, midX + xOffset + 4, yOffset + 23, rewardColor);

        xo = midX + xOffset + 80;
        yo = yOffset + 20;

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

        if (!rendereredOfferButtons) {
            var button = new QuestOffersScreen.AcceptQuestButton(midX + 83, yOffset + 8, b -> {
            });

            screen.addRenderableWidget(button);
            rendereredOfferButtons = true;
        }
    }
}
