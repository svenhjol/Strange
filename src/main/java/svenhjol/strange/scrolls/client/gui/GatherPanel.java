package svenhjol.strange.scrolls.client.gui;

import net.minecraft.item.ItemStack;
import svenhjol.strange.scrolls.client.QuestIcons;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.action.Gather;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.List;

public class GatherPanel extends BasePanel
{
    public GatherPanel(IQuest quest, int mid, int y, int width)
    {
        super(quest, width);

        List<Condition<Gather>> toGather = quest.getCriteria().getConditions(Gather.class);

        if (toGather.isEmpty()) return;
        drawBackground(mid - width, mid + width, y, y + 16 + (toGather.size() * 16) + pad);

        // draw title
        y += pad; // space above title
        this.drawCenteredString(fonts, "Gather", mid, y, primaryTextColor);
        y += 16; // space between title and item list

        for (int i = 0; i < toGather.size(); i++) {
            y += i * 16; // height of each line

            Gather gather = toGather.get(i).getDelegate();
            ItemStack stack = gather.getStack();
            int remaining = gather.getRemaining();

            // draw remaining count and item icon
            this.drawRightAlignedString(fonts, String.valueOf(remaining), mid - 30, y, primaryTextColor);
            items.renderItemIntoGUI(stack, mid - 14, y - 5);
            this.drawString(fonts, stack.getDisplayName().getString(), mid + 4, y, primaryTextColor);

            // show tick if complete
            if (remaining == 0) blitIcon(mid - 20, y, QuestIcons.ICON_TICK);
        }
    }
}
