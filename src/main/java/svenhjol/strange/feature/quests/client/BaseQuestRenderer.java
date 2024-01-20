package svenhjol.strange.feature.quests.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import svenhjol.charmony.helper.TextHelper;
import svenhjol.strange.feature.quests.Quest;
import svenhjol.strange.feature.quests.reward.RewardItem;
import svenhjol.strange.feature.quests.reward.RewardXp;

public abstract class BaseQuestRenderer<Q extends Quest<?>> {
    Q quest;
    int midX;
    int titleColor;
    int requirementColor;
    int satisfiedColor;
    int rewardColor;
    int requirementBackgroundColor;
    int satisfiedBackgroundColor;
    int rewardBackgroundColor;
    int offerBorderColor;
    String remainingKey;
    Button.OnPress onAccept;
    Button.OnPress onUpdate;
    Button.OnPress onAbandon;

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
        titleColor = quest.isEpic() ? 0xffe0a0 : 0xffffff;
        requirementColor = quest.isEpic() ? 0xdfc080 : 0xa0a0a0;
        satisfiedColor = 0xa0ffa0;
        rewardColor = 0xa0ffff;
        requirementBackgroundColor = quest.isEpic() ? 0x24dfc080 : 0x24a0a0a0;
        satisfiedBackgroundColor = 0x24a0ffa0;
        rewardBackgroundColor = 0x24a0ffff;
        offerBorderColor = quest.isEpic() ? 0x40ffe080 : 0x40a0a0a0;
        remainingKey = "gui.strange.quests.amount";
    }

    protected void activeTheme() {
        titleColor = 0x202020;
        requirementColor = 0x303030;
        satisfiedColor = 0x308030;
        rewardColor = 0x308080;
        requirementBackgroundColor = 0x24000000;
        satisfiedBackgroundColor = 0x24008000;
        rewardBackgroundColor = 0x24008080;
        remainingKey = "gui.strange.quests.remaining";
    }

    protected void init(Screen screen) {
        midX = screen.width / 2;
    }

    public Q quest() {
        return quest;
    }

    public void setQuest(Q quest) {
        this.quest = quest;
    }

    public int getPagedOfferHeight() {
        return 74;
    }

    public int getPagedActiveHeight() {
        return 40;
    }

    public void renderPagedOffer(Screen screen, GuiGraphics guiGraphics, int yOffset, int mouseX, int mouseY) {
        // no op
    }

    public void renderPagedActive(Screen screen, GuiGraphics guiGraphics, int yOffset, int mouseX, int mouseY) {
        // no op
    }

    public void renderSelectedActive(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // no op
    }

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
