package svenhjol.strange.scrolls.client.gui;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.item.ItemStack;
import svenhjol.strange.scrolls.quest.IQuest;
import svenhjol.strange.scrolls.quest.action.Action;
import svenhjol.strange.scrolls.quest.action.Gather;

import java.util.List;

public class GatherQuestPanel extends BaseQuestPanel
{
    public GatherQuestPanel(IQuest quest, int width, int x, int y)
    {
        super(quest, width);
        int mid = x + (width / 2);
        int hpad = 8;

        List<Action<Gather>> actions = quest.getCriteria().getActions(Gather.class);

        if (!actions.isEmpty()) {

            // draw background rect
            int x0 = mid - 100;
            int y0 = y;
            int x1 = mid + 100;
            int y1 = y + 16 + (actions.size() * 16) + hpad;
            AbstractGui.fill(x0, y0, x1, y1, 0x88000000);

            // draw title
            y += hpad; // space above title
            this.drawCenteredString(fonts, "Gather", mid, y, primaryTextColor);
            y += 16; // space between title and item list

            for (int i = 0; i < actions.size(); i++) {
                y += i * 16; // height of each line

                Gather gather = actions.get(i).getDelegate();
                ItemStack stack = gather.getStack();
                int remaining = gather.getCount() - gather.getCollected();

                // draw remaining count and item icon
                this.drawRightAlignedString(fonts, String.valueOf(remaining), mid - 30, y, primaryTextColor);
                items.renderItemIntoGUI(stack, mid - 14, y - 5);
                this.drawString(fonts, stack.getDisplayName().getString(), mid + 4, y, primaryTextColor);

//                if (remaining == 0) {
//                  renderIcon(AbstractGui.GUI_ICONS_LOCATION, 10, y, 100);
//                }
            }
        }
    }
}
