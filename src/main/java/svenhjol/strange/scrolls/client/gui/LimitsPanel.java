package svenhjol.strange.scrolls.client.gui;

import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.iface.ICondition;
import svenhjol.strange.scrolls.quest.limit.Time;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.List;

public class LimitsPanel extends BasePanel
{
    public LimitsPanel(IQuest quest, int mid, int y, int width)
    {
        super(quest, width);

        final List<Condition<ICondition>> limits = quest.getCriteria().getConditions("limit");
        if (limits.isEmpty()) return;

        drawBackground(mid - width, mid + width, y, y + 30);
        y += pad;

        for (Condition limit : limits) {
            if (limit.getDelegate() instanceof Time) {
                // render time row
                Time time = (Time)limit.getDelegate();
                String out = "";

                final long remaining = (time.getRemaining() / 20);

                if (remaining > 60) {
                    int mins = (int) Math.floor((float)remaining / 60);
                    int secs = (int)(remaining % 60);

                    final String minsStr = String.valueOf(mins);
                    final String secsStr = (secs < 10 ? "0" : "") + secs;

                    out = "Complete within " + minsStr + ":" + secsStr;
                } else {
                    out = "Complete within " + remaining + " seconds";
                }

                blitItemIcon(mid - 72, y, "clock_13");
                this.drawString(fonts, out, mid - 60, y, primaryTextColor);
            }
        }
    }
}
