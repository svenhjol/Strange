package svenhjol.strange.scrolls.quest.panel;

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

public class RewardsPanel extends BasePanel
{
    public static final int ROW_HEIGHT = 18; // height of each line

    public RewardsPanel(IQuest quest, int mid, int y, int width)
    {
        super(quest, mid, width);

        List<Condition<IDelegate>> rewards = quest.getCriteria().getConditions(Criteria.REWARD);
        if (rewards.isEmpty()) return;

        y += pad; // space above title

        // draw title
        drawCenteredTitle(I18n.format("gui.strange.quests.rewards"), y);

        for (int i = 0; i < rewards.size(); i++) {
            y += ROW_HEIGHT;

            Condition condition = rewards.get(i);

            if (condition.getDelegate() instanceof Reward) {
                Reward reward = (Reward)condition.getDelegate();
                final int xp = reward.getXP();
                final Map<ItemStack, Integer> rewardItems = reward.getItems();

                if (xp > 0) {
                    // render XP
                    String out = I18n.format("gui.strange.quests.reward_xp", xp);
                    blitItemIcon(new ItemStack(Items.EXPERIENCE_BOTTLE), mid - 60, y - 5);
                    this.drawString(fonts, out, mid - 36, y, primaryTextColor);
                    y += ROW_HEIGHT;
                }

                if (rewardItems.size() > 0) {
                    // render items
                    for (ItemStack stack : rewardItems.keySet()) {
                        int count = rewardItems.get(stack);
                        String out = I18n.format("gui.strange.quests.reward_item", count, stack.getDisplayName().getString());
                        blitItemIcon(stack, mid - 60, y - 5);
                        this.drawString(fonts, out, mid - 36, y, primaryTextColor);
                        y += ROW_HEIGHT;
                    }
                }
            }
        }
    }
}
