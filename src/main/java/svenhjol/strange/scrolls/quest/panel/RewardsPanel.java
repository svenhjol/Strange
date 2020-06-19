package svenhjol.strange.scrolls.quest.panel;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.condition.Reward;
import svenhjol.strange.scrolls.quest.iface.IDelegate;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class RewardsPanel extends BasePanel {
    public RewardsPanel(Screen screen, IQuest quest, int mid, int y, int width, int mouseX, int mouseY) {
        super(screen, quest, mid, width);

        List<Condition<IDelegate>> rewards = quest.getCriteria().getConditions(Criteria.REWARD);
        if (rewards.isEmpty()) return;

        y += pad; // space above title

        // draw title
        drawCenteredTitle(I18n.format("gui.strange.quests.rewards"), y);

        for (Condition<IDelegate> iDelegateCondition : rewards) {
            y += rowHeight;

            if (iDelegateCondition.getDelegate() instanceof Reward) {
                Reward reward = (Reward) iDelegateCondition.getDelegate();
                final int xp = reward.getXP();
                final Map<ItemStack, Integer> rewardItems = reward.getItems();

                if (xp > 0) {
                    // render XP
                    String out = I18n.format("gui.strange.quests.reward_xp", xp);
                    blitItemIcon(new ItemStack(Items.EXPERIENCE_BOTTLE), mid - 60, y - 5);
                    this.drawString(fonts, out, mid - 36, y, primaryTextColor);
                    y += rowHeight;
                }

                if (rewardItems.size() > 0) {
                    Set<ItemStack> stacks = rewardItems.keySet();

                    // render item names
                    int cy = y;
                    for (ItemStack stack : stacks) {
                        int count = rewardItems.get(stack);
                        String out = I18n.format("gui.strange.quests.reward_item", stack.getDisplayName().getString(), count);
                        blitItemIcon(stack, mid - 60, cy - 5);
                        this.drawString(fonts, out, mid - 36, cy, primaryTextColor);
                        cy += rowHeight;
                    }

                    // render item tooltips
                    cy = y;
                    for (ItemStack stack : stacks) {
                        if (!stack.hasEffect())
                            continue;

                        if (mouseX > mid - 60 && mouseX < mid - 44
                            && mouseY > cy - 5 && mouseY < cy + 11) {
                            screen.renderTooltip(screen.getTooltipFromItem(stack), mouseX, mouseY);
                        }
                        cy += rowHeight;
                    }
                }
            }
        }
    }
}
