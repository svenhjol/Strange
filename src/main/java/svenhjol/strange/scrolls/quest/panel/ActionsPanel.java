package svenhjol.strange.scrolls.quest.panel;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import svenhjol.strange.scrolls.client.QuestIcons;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.condition.Craft;
import svenhjol.strange.scrolls.quest.condition.Gather;
import svenhjol.strange.scrolls.quest.condition.Hunt;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.List;

public class ActionsPanel extends BasePanel
{
    public ActionsPanel(IQuest quest, String actionId, int mid, int y, int width)
    {
        super(quest, mid, width);

        // TODO gammy switch needs replacing with something better
        switch (actionId) {
            case Gather.ID:
                new GatherPanel(quest, mid, y, width);
                break;

            case Craft.ID:
                new CraftPanel(quest, mid, y, width);
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
            drawCenteredTitle(I18n.format("gui.strange.quests.gather"), y);
            y += 16; // space between title and item list

            for (int i = 0; i < toGather.size(); i++) {
                y += i * 16; // height of each line

                Gather gather = toGather.get(i).getDelegate();
                ItemStack stack = gather.getStack();
                int remaining = gather.getRemaining();

                // draw remaining count and item icon
                blitItemIcon(stack, mid - 60, y - 5);
                this.drawString(fonts, remaining + " " + stack.getDisplayName().getString(), mid - 36, y, primaryTextColor);

                // show tick if complete
                if (remaining == 0) blitIcon(QuestIcons.ICON_TICK, mid - 70, y - 1);
            }
        }
    }

    public static class CraftPanel extends BasePanel
    {
        public CraftPanel(IQuest quest, int mid, int y, int width)
        {
            super(quest, mid, width);
            List<Condition<Craft>> toCraft = quest.getCriteria().getConditions(Craft.class);
            if (toCraft.isEmpty()) return;

            y += pad; // space above title

            // draw title
            drawCenteredTitle(I18n.format("gui.strange.quests.craft"), y);
            y += 16; // space between title and item list

            for (int i = 0; i < toCraft.size(); i++) {
                y += i * 16; // height of each line

                Craft craft = toCraft.get(i).getDelegate();
                ItemStack stack = craft.getStack();
                int remaining = craft.getRemaining();

                // draw remaining count and item icon
                blitItemIcon(stack, mid - 60, y - 5);
                this.drawString(fonts, remaining + " " + stack.getDisplayName().getString(), mid - 36, y, primaryTextColor);

                // show tick if complete
                if (remaining == 0) blitIcon(QuestIcons.ICON_TICK, mid - 70, y - 1);
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
            drawCenteredTitle(I18n.format("gui.strange.quests.hunt"), y);
            y += 16; // space between title and item list

            for (int i = 0; i < actions.size(); i++) {
                y += i * 16; // height of each line

                Hunt hunt = actions.get(i).getDelegate();
                ResourceLocation target = hunt.getTarget();
                int remaining = hunt.getCount() - hunt.getKilled();
                EntityType<?> entity = Registry.ENTITY_TYPE.getOrDefault(target);

                // draw remaining count and item icon
                blitItemIcon(new ItemStack(Items.IRON_SWORD), mid - 60, y - 5);
                this.drawString(fonts, remaining + " " + entity.getName().getFormattedText(), mid - 36, y, primaryTextColor);

                // show tick if complete
                if (remaining == 0) blitIcon(QuestIcons.ICON_TICK, mid - 70, y - 1);
            }
        }
    }
}
