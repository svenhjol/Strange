package svenhjol.strange.feature.quests.gather;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.quests.QuestsResources;
import svenhjol.strange.feature.quests.client.BaseQuestRenderer;

public class GatherQuestRenderer extends BaseQuestRenderer<GatherQuest> {
    @Override
    public int getPagedOfferHeight() {
        return super.getPagedOfferHeight();
    }

    @Override
    public int getPagedActiveHeight() {
        return super.getPagedActiveHeight() + 10;
    }

    @Override
    public Component getRequirementText() {
        return QuestsResources.GATHER_REQUIREMENT_TEXT;
    }

    @Override
    public Component getRewardText() {
        return QuestsResources.GATHER_REWARD_TEXT;
    }

    @Override
    public ItemStack getQuestIcon() {
        return new ItemStack(Items.BUNDLE);
    }

    @Override
    protected void renderRequirements(GuiGraphics guiGraphics, int xOffset, int yOffset, int mouseX, int mouseY) {
        var font = screen.font;

        var xo = midX + xOffset;
        var yo = yOffset;

        for (var requirement : quest.requirements()) {
            if (requirement instanceof GatherQuest.GatherItem i) {
                Component label;
                if (showRemaining) {
                    label = TextHelper.translatable("gui.strange.quests.remaining", Math.max(0, i.total() - i.remaining()), i.total());
                } else {
                    label = TextHelper.translatable("gui.strange.quests.amount", i.total());
                }

                int width;

                switch (screenType) {
                    case PAGED_ACTIVE, SELECTED_ACTIVE:
                        guiGraphics.drawString(font, label, xo + 4, yo + 20, i.satisfied() ? satisfiedColor : requirementColor, false);
                        width = font.width(label);

                        guiGraphics.fill(xo, yo - 2, xo + width + 6, yo + 30, i.satisfied() ? satisfiedBackgroundColor : requirementBackgroundColor);
                        guiGraphics.renderFakeItem(i.item, xo + (width / 2) - 5, yo);

                        if (mouseX >= xo + (width / 2) - 5 && mouseX <= xo + (width / 2) + 10
                            && mouseY >= yo && mouseY <= yo + 16) {
                            guiGraphics.renderTooltip(font, i.item, xo, yo + 27);
                        }

                        xo += 8 + width;
                        break;

                    case PAGED_OFFER:
                        guiGraphics.drawString(font, label, xo + 3, yo + 5, i.satisfied() ? satisfiedColor : requirementColor, false);
                        width = font.width(label);

                        guiGraphics.fill(xo, yo - 1, xo + 22 + width, yo + 17, i.satisfied() ? satisfiedBackgroundColor : requirementBackgroundColor);
                        guiGraphics.renderFakeItem(i.item, xo + 5 + width, yo);

                        if (mouseX >= xo && mouseX <= xo + 25 + width
                            && mouseY >= yo - 1 && mouseY <= yo + 17) {
                            guiGraphics.renderTooltip(font, i.item, xo, yo + 27);
                        }

                        xo += 24 + width;
                        break;
                }
            }
        }

        renderSatisfied(guiGraphics, xo, yo + 5, mouseX, mouseY);
    }
}
