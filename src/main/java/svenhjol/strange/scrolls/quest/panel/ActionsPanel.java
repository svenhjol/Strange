package svenhjol.strange.scrolls.quest.panel;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import svenhjol.strange.scrolls.client.QuestIcons;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.condition.*;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.List;

@SuppressWarnings("deprecation")
public class ActionsPanel extends BasePanel {
    public ActionsPanel(IQuest quest, String actionId, int mid, int y, int width) {
        super(quest, mid, width);

        // TODO gammy switch needs replacing with something better
        switch (actionId) {
            case Gather.ID:
                new GatherPanel(quest, mid, y, width);
                break;

            case Craft.ID:
                new CraftPanel(quest, mid, y, width);
                break;

            case Encounter.ID:
                new EncounterPanel(quest, mid, y, width);
                break;

            case Hunt.ID:
                new HuntPanel(quest, mid, y, width);
                break;

            case Mine.ID:
                new MinePanel(quest, mid, y, width);
                break;

            case Locate.ID:
                new LocatePanel(quest, mid, y, width);
                break;

            case Fetch.ID:
                new FetchPanel(quest, mid, y, width);
                break;

            default:
                // shrug
                break;
        }
    }

    public static class GatherPanel extends BasePanel {
        public GatherPanel(IQuest quest, int mid, int y, int width) {
            super(quest, mid, width);
            List<Condition<Gather>> toGather = quest.getCriteria().getConditions(Gather.class);
            if (toGather.isEmpty()) return;

            y += pad; // space above title

            // draw title
            drawCenteredTitle(I18n.format("gui.strange.quests.gather"), y);

            for (Condition<Gather> gatherCondition : toGather) {
                y += rowHeight;

                Gather gather = gatherCondition.getDelegate();
                ItemStack stack = gather.getStack();
                int remaining = gather.getRemaining();

                // draw remaining count and item icon
                blitItemIcon(stack, mid - 60, y - 5);
                this.drawString(fonts, stack.getDisplayName().getString() + ": " + remaining, mid - 36, y, primaryTextColor);

                // show tick if complete
                if (remaining == 0) blitIcon(QuestIcons.ICON_TICK, mid - 70, y - 1);
            }
        }
    }

    public static class CraftPanel extends BasePanel {
        public CraftPanel(IQuest quest, int mid, int y, int width) {
            super(quest, mid, width);
            List<Condition<Craft>> toCraft = quest.getCriteria().getConditions(Craft.class);
            if (toCraft.isEmpty()) return;

            y += pad; // space above title

            // draw title
            drawCenteredTitle(I18n.format("gui.strange.quests.craft"), y);

            for (Condition<Craft> craftCondition : toCraft) {
                y += rowHeight;

                Craft craft = craftCondition.getDelegate();
                ItemStack stack = craft.getStack();
                int remaining = craft.getRemaining();

                // draw remaining count and item icon
                blitItemIcon(stack, mid - 60, y - 5);
                this.drawString(fonts, stack.getDisplayName().getString() + ": " + remaining, mid - 36, y, primaryTextColor);

                // show tick if complete
                if (remaining == 0) blitIcon(QuestIcons.ICON_TICK, mid - 70, y - 1);
            }
        }
    }

    public static class MinePanel extends BasePanel {
        public MinePanel(IQuest quest, int mid, int y, int width) {
            super(quest, mid, width);
            List<Condition<Mine>> toMine = quest.getCriteria().getConditions(Mine.class);
            if (toMine.isEmpty()) return;

            y += pad; // space above title

            // draw title
            drawCenteredTitle(I18n.format("gui.strange.quests.mine"), y);

            for (Condition<Mine> mineCondition : toMine) {
                y += rowHeight;

                Mine mine = mineCondition.getDelegate();
                Block block = mine.getBlock();
                int remaining = mine.getRemaining();

                // draw remaining count and block name
                blitItemIcon(new ItemStack(Items.IRON_PICKAXE), mid - 60, y - 5);
                this.drawString(fonts, block.getNameTextComponent().getString() + ": " + remaining, mid - 36, y, primaryTextColor);

                // show tick if complete
                if (remaining == 0) blitIcon(QuestIcons.ICON_TICK, mid - 70, y - 1);
            }
        }
    }

    public static class HuntPanel extends BasePanel {
        public HuntPanel(IQuest quest, int mid, int y, int width) {
            super(quest, mid, width);

            List<Condition<Hunt>> actions = quest.getCriteria().getConditions(Hunt.class);
            if (actions.isEmpty()) return;

            y += pad; // space above title

            // draw title
            drawCenteredTitle(I18n.format("gui.strange.quests.hunt"), y);

            for (Condition<Hunt> action : actions) {
                y += rowHeight;

                Hunt hunt = action.getDelegate();
                ResourceLocation target = hunt.getTarget();
                int remaining = hunt.getCount() - hunt.getKilled();
                EntityType<?> entity = Registry.ENTITY_TYPE.getOrDefault(target);

                // draw remaining count and item icon
                blitItemIcon(new ItemStack(Items.IRON_SWORD), mid - 60, y - 5);
                this.drawString(fonts, entity.getName().getFormattedText() + ": " + remaining, mid - 36, y, primaryTextColor);

                // show tick if complete
                if (remaining == 0) blitIcon(QuestIcons.ICON_TICK, mid - 70, y - 1);
            }
        }
    }

    public static class EncounterPanel extends BasePanel {
        public EncounterPanel(IQuest quest, int mid, int y, int width) {
            super(quest, mid, width);

            List<Condition<Encounter>> actions = quest.getCriteria().getConditions(Encounter.class);
            if (actions.isEmpty()) return;

            y += pad; // space above title

            // draw title
            drawCenteredTitle(I18n.format("gui.strange.quests.encounter"), y);

            for (Condition<Encounter> action : actions) {
                y += rowHeight;

                Encounter encounter = action.getDelegate();
                int remaining = encounter.getCount() - encounter.getKilled();

                // draw remaining count and item icon
                blitItemIcon(new ItemStack(Items.DIAMOND_SWORD), mid - 60, y - 5);
                this.drawString(fonts, I18n.format("gui.strange.quests.encounter_defeat"), mid - 36, y, primaryTextColor);

                // show tick if complete
                if (encounter.isSatisfied()) blitIcon(QuestIcons.ICON_TICK, mid - 70, y - 1);
            }
        }
    }

    public static class LocatePanel extends BasePanel {
        public LocatePanel(IQuest quest, int mid, int y, int width) {
            super(quest, mid, width);
            List<Condition<Locate>> toLocate = quest.getCriteria().getConditions(Locate.class);
            if (toLocate.isEmpty()) return;

            y += pad; // space above title

            // draw title
            drawCenteredTitle(I18n.format("gui.strange.quests.locate"), y);

            for (Condition<Locate> locateCondition : toLocate) {
                y += rowHeight;

                Locate locate = locateCondition.getDelegate();
                ItemStack stack = locate.getStack();

                // draw remaining count and item icon
                blitItemIcon(stack, mid - 60, y - 5);
                this.drawString(fonts, stack.getDisplayName().getString(), mid - 36, y, primaryTextColor);

                // show tick if complete
                if (locate.isSatisfied()) blitIcon(QuestIcons.ICON_TICK, mid - 70, y - 1);
            }
        }
    }

    public static class FetchPanel extends BasePanel {
        public FetchPanel(IQuest quest, int mid, int y, int width) {
            super(quest, mid, width);

            List<Condition<Fetch>> actions = quest.getCriteria().getConditions(Fetch.class);
            if (actions.isEmpty()) return;

            y += pad; // space above title

            // draw title
            drawCenteredTitle(I18n.format("gui.strange.quests.fetch"), y);

            for (Condition<Fetch> action : actions) {
                y += rowHeight;

                Fetch fetch = action.getDelegate();
                ResourceLocation target = fetch.getTarget();
                int remaining = fetch.getCount() - fetch.getFetched();
                EntityType<?> entity = Registry.ENTITY_TYPE.getOrDefault(target);

                // draw remaining count and item icon
                blitItemIcon(new ItemStack(Items.LEAD), mid - 60, y - 5);
                this.drawString(fonts, entity.getName().getFormattedText() + ": " + remaining, mid - 36, y, primaryTextColor);

                // show tick if complete
                if (remaining == 0) blitIcon(QuestIcons.ICON_TICK, mid - 70, y - 1);

                // show position
                if (remaining > 0 && quest.getState() == IQuest.State.Started) {
                    y += rowHeight;
                    final BlockPos pos = fetch.getLocation();
                    this.drawString(fonts, I18n.format("gui.strange.quests.fetch_origin", pos.getX(), pos.getZ()), mid - 36, y, secondaryTextColor);
                }
            }
        }
    }
}
