package svenhjol.strange.scrolls.quest.panel;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.iface.IDelegate;
import svenhjol.strange.scrolls.quest.condition.Time;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.List;

public class LimitsPanel extends BasePanel
{
    public LimitsPanel(IQuest quest, int mid, int y, int width)
    {
        super(quest, mid, width);

        final List<Condition<IDelegate>> limits = quest.getCriteria().getConditions(Criteria.LIMIT);
        if (limits.isEmpty()) return;

        y += pad; // space above title

        // draw title
        drawCenteredTitle(I18n.format("gui.strange.quests.conditions"), y);

        for (Condition limit : limits) {
            y += 18; // space between title and item list

            if (limit.getDelegate() instanceof Time) {
                Time time = (Time)limit.getDelegate();
                String out;
                final long remaining = (time.getRemaining() / 20);

                if (remaining > 60) {
                    int mins = (int) Math.floor((float)remaining / 60);
                    int secs = (int)(remaining % 60);

                    final String minsStr = String.valueOf(mins);
                    final String secsStr = (secs < 10 ? "0" : "") + secs;
                    out = I18n.format("gui.strange.quests.complete_within_time", minsStr, secsStr);
                } else {
                    out = I18n.format("gui.strange.quests.complete_within_seconds", remaining);
                }

                blitItemIcon(new ItemStack(Items.CLOCK), mid - 60, y - 5);
                this.drawString(fonts, out, mid - 36, y, primaryTextColor);
            }
        }
    }
}
