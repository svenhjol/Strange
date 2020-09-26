package svenhjol.strange.scroll.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableText;
import svenhjol.strange.scroll.BasePanel;
import svenhjol.strange.scroll.tag.QuestTag;
import svenhjol.strange.scroll.tag.RewardTag;

import java.util.ArrayList;
import java.util.Map;

public class RewardPanel extends BasePanel {
    public static RewardPanel INSTANCE = new RewardPanel();

    public void render(Screen screen, MatrixStack matrices, QuestTag quest, int mid, int width, int top, int mouseX, int mouseY) {
        RewardTag reward = quest.getReward();
        Map<ItemStack, Integer> items = reward.getItems();
        int xp = reward.getXp();

        // the panel title
        drawCenteredTitle(matrices, I18n.translate("gui.strange.scrolls.reward"), mid, top, titleColor);

        top += rowHeight;

        // if the reward has XP then show it here
        if (xp > 0) {
            TranslatableText text = new TranslatableText("gui.strange.quests.reward_xp", xp);
            renderItemIcon(new ItemStack(Items.EXPERIENCE_BOTTLE), mid - 60, top - 5);
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
                TranslatableText text = new TranslatableText("gui.strange.quests.reward_item", stack.getName(), count);
                renderItemIcon(stack, mid - 60, baseTop - 5);
                drawTextWithShadow(matrices, getTextRenderer(), text, mid - 36, baseTop, textColor);
                baseTop += rowHeight;
            }

            // tooltips
            baseTop = top;
            for (ItemStack stack : stacks) {
                if (stack.hasGlint()
                    && mouseX > mid - 60 && mouseX < mid - 44
                    && mouseY > baseTop - 5 && mouseY < baseTop + 11
                ) {
                    screen.renderTooltip(matrices, screen.getTooltipFromItem(stack), mouseX, mouseY);
                }
                baseTop += rowHeight;
            }
        }
    }
}
