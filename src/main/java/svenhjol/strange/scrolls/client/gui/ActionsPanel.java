package svenhjol.strange.scrolls.client.gui;

import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import svenhjol.strange.scrolls.client.QuestIcons;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.action.Gather;
import svenhjol.strange.scrolls.quest.action.Hunt;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.List;

public class ActionsPanel extends BasePanel
{
    public ActionsPanel(IQuest quest, String actionId, int mid, int y, int width)
    {
        super(quest, mid, width);

        // gammy switch needs replacing with something better
        switch (actionId) {
            case Gather.ID:
                new GatherPanel(quest, mid, y, width);
                break;

            case Hunt.ID:
                new HuntPanel(quest, mid, y, width);
                break;

            default:
                // shrug
                break;
        }
    }

    public static class GatherPanel extends BasePanel
    {
        public GatherPanel(IQuest quest, int mid, int y, int width)
        {
            super(quest, mid, width);

            List<Condition<Gather>> toGather = quest.getCriteria().getConditions(Gather.class);
            if (toGather.isEmpty()) return;

            y += pad; // space above title

            // draw title
            drawCenteredTitle("Pickup items", y);
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
                if (remaining == 0) blitIcon(QuestIcons.ICON_TICK, mid - 24, y - 1);
            }
        }
    }

    public static class HuntPanel extends BasePanel
    {
        public HuntPanel(IQuest quest, int mid, int y, int width)
        {
            super(quest, mid, width);

            List<Condition<Hunt>> actions = quest.getCriteria().getConditions(Hunt.class);
            if (actions.isEmpty()) return;

            y += pad; // space above title

            // draw title
            drawCenteredTitle("Kill mobs", y);
            y += 16; // space between title and item list

            for (int i = 0; i < actions.size(); i++) {
                y += i * 16; // height of each line

                Hunt hunt = actions.get(i).getDelegate();
                ResourceLocation target = hunt.getTarget();
                int remaining = hunt.getCount() - hunt.getKilled();

                // draw remaining count and item icon
                this.drawRightAlignedString(fonts, String.valueOf(remaining), mid - 30, y, primaryTextColor);
                EntityType<?> entity = Registry.ENTITY_TYPE.getOrDefault(target);
                blitItemIcon(new ItemStack(Items.IRON_SWORD), mid - 72, y);
                this.drawString(fonts, entity.getName().getFormattedText(), mid + 4, y, primaryTextColor);

                // show tick if complete
                if (remaining == 0) blitIcon(QuestIcons.ICON_TICK, mid - 8, y - 1);
            }
        }
    }
}
