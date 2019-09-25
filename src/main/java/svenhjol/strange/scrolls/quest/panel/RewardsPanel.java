package svenhjol.strange.scrolls.quest.panel;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.condition.RewardItem;
import svenhjol.strange.scrolls.quest.condition.XP;
import svenhjol.strange.scrolls.quest.iface.IDelegate;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.List;

public class RewardsPanel extends BasePanel
{
    public RewardsPanel(IQuest quest, int mid, int y, int width)
    {
        super(quest, mid, width);

        List<Condition<IDelegate>> rewards = quest.getCriteria().getConditions(Criteria.REWARD);
        if (rewards.isEmpty()) return;

        y += pad; // space above title

        // draw title
        drawCenteredTitle(I18n.format("gui.strange.quests.rewards"), y);

        for (int i = 0; i < rewards.size(); i++) {
            y += 18; // height of each line

            Condition reward = rewards.get(i);

            if (reward.getDelegate() instanceof XP) {
                // render XP row
                XP xp = (XP)reward.getDelegate();
                final int amount = xp.getAmount();
                String out = I18n.format("gui.strange.quests.experience", amount);

                blitItemIcon(new ItemStack(Items.EXPERIENCE_BOTTLE), mid - 60, y - 5);
                this.drawString(fonts, out, mid - 36, y, primaryTextColor);
            }

            if (reward.getDelegate() instanceof RewardItem) {
                // render item
                RewardItem rewardItem = (RewardItem)reward.getDelegate();
                ItemStack stack = rewardItem.getStack();
                int amount = rewardItem.getCount();

                items.renderItemIntoGUI(stack, mid - 60, y - 5);
                this.drawString(fonts, amount + " " + stack.getDisplayName().getString(), mid - 36, y, primaryTextColor);
            }
        }
    }
}
