package svenhjol.strange.scrolls.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableText;
import svenhjol.strange.base.StrangeIcons;
import svenhjol.strange.scrolls.tag.Quest;
import svenhjol.strange.scrolls.tag.Reward;

import java.util.ArrayList;
import java.util.Map;

public class RewardPanel extends Panel {
    public static RewardPanel INSTANCE = new RewardPanel();

    public void render(Screen screen, MatrixStack matrices, Quest quest, int mid, int width, int top, int mouseX, int mouseY) {
        Reward reward = quest.getReward();
        Map<ItemStack, Integer> items = reward.getItems();
        int levels = reward.getPlayerXp();

        if (items.size() == 0 && levels == 0)
            return; // no reward for you, goodbye :(

        // panel title and icon
        TranslatableText titleText = new TranslatableText("gui.strange.scrolls.reward");
        drawCenteredTitle(matrices, titleText, mid, top, titleColor);
        renderIcon(matrices, StrangeIcons.ICON_STAR, mid - 14 - (getTextRenderer().getWidth(titleText) / 2), top - 1);

        top += rowHeight;

        // if the reward provides XP levels then show it here
        if (levels > 0) {
            TranslatableText text = new TranslatableText("gui.strange.scrolls.reward_levels", levels);
            renderItemStack(new ItemStack(Items.EXPERIENCE_BOTTLE), mid - 60, top - 5);
            drawTextWithShadow(matrices, getTextRenderer(), text, mid - 36, top, textColor);
            top += rowHeight;
        }

        // if there are items, render them out with icon, name and quantity
        if (items.size() > 0) {
            ArrayList<ItemStack> stacks = new ArrayList<>(items.keySet());

            // names
            int baseTop = top;
            for (ItemStack stack : stacks) {
                int count = items.get(stack);
                TranslatableText text = new TranslatableText("gui.strange.scrolls.reward_item", stack.getName(), count);
                renderItemStack(stack, mid - 60, baseTop - 5);
                drawTextWithShadow(matrices, getTextRenderer(), text, mid - 36, baseTop, textColor);
                baseTop += rowHeight;
            }

            // tooltips
            baseTop = top;
            for (ItemStack stack : stacks) {
                if (mouseX > mid - 60 && mouseX < mid - 44
                    && mouseY > baseTop - 5 && mouseY < baseTop + 11
                ) {
                    screen.renderTooltip(matrices, screen.getTooltipFromItem(stack), mouseX, mouseY);
                }
                baseTop += rowHeight;
            }
        }
    }
}
