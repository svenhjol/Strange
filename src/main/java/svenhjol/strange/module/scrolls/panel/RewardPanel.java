package svenhjol.strange.module.scrolls.panel;

import svenhjol.strange.init.StrangeIcons;
import svenhjol.strange.module.scrolls.tag.Quest;
import svenhjol.strange.module.scrolls.tag.Reward;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Map;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RewardPanel extends BasePanel {
    public static RewardPanel INSTANCE = new RewardPanel();

    public void render(Screen screen, PoseStack matrices, Quest quest, int mid, int width, int top, int mouseX, int mouseY) {
        Reward reward = quest.getReward();
        Map<ItemStack, Integer> items = reward.getItems();
        int levels = reward.getPlayerXp();

        if (items.size() == 0 && levels == 0)
            return; // no reward for you, goodbye :(

        // panel title and icon
        TranslatableComponent titleText = new TranslatableComponent("gui.strange.scrolls.reward");
        drawString(matrices, getTextRenderer(), titleText, mid - 44, top, titleColor);
        renderIcon(matrices, StrangeIcons.ICON_STAR, mid - 60, top - 1);

        top += rowHeight;

        // if the reward provides XP levels then show it here
        if (levels > 0) {
            TranslatableComponent text = new TranslatableComponent("gui.strange.scrolls.reward_levels", levels);
            renderItemStack(new ItemStack(Items.EXPERIENCE_BOTTLE), mid - 60, top - 5);
            drawString(matrices, getTextRenderer(), text, mid - 36, top, textColor);
            top += rowHeight;
        }

        // if there are items, render them out with icon, name and quantity
        if (items.size() > 0) {
            ArrayList<ItemStack> stacks = new ArrayList<>(items.keySet());

            // names
            int baseTop = top;
            for (ItemStack stack : stacks) {
                int count = items.get(stack);
                TranslatableComponent text = new TranslatableComponent("gui.strange.scrolls.reward_item", stack.getHoverName(), count);
                renderItemStack(stack, mid - 60, baseTop - 5);
                drawString(matrices, getTextRenderer(), text, mid - 36, baseTop, textColor);
                baseTop += rowHeight;
            }

            // tooltips
            baseTop = top;
            for (ItemStack stack : stacks) {
                if (mouseX > mid - 60 && mouseX < mid - 44
                    && mouseY > baseTop - 5 && mouseY < baseTop + 11
                ) {
                    screen.renderComponentTooltip(matrices, screen.getTooltipFromItem(stack), mouseX, mouseY);
                }
                baseTop += rowHeight;
            }
        }
    }
}
