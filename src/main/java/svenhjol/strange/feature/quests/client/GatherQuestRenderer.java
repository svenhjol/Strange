package svenhjol.strange.feature.quests.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.quests.QuestResources;
import svenhjol.strange.feature.quests.QuestsNetwork;
import svenhjol.strange.feature.quests.quest.GatherQuest;

public class GatherQuestRenderer extends BaseQuestRenderer<GatherQuest> {
    boolean rendereredOfferButtons = false;

    public GatherQuestRenderer() {
    }

    @Override
    public int getOfferHeight() {
        return 74;
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

        if (!rendereredOfferButtons) {
            var button = new QuestOffersScreen.AcceptQuestButton(midX + 83, yOffset + 26, b -> {
                QuestsNetwork.AcceptQuest.send(quest.villagerUuid(), quest.id());
                screen.onClose();
            });

            screen.addRenderableWidget(button);
            rendereredOfferButtons = true;
        }
    }

    protected Component makeTitle() {
        return TextHelper.translatable(QuestResources.QUEST_TITLE_KEY,
            TextHelper.translatable("merchant.level." + quest.villagerLevel()),
            TextHelper.translatable("gui.strange.quests.gathering"));
    }
}
