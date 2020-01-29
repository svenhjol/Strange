package svenhjol.strange.scrolls.quest.condition;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.meson.helper.PlayerHelper;
import svenhjol.strange.scrolls.event.QuestEvent;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.iface.IDelegate;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class Reward implements IDelegate
{
    public static final String ID = "Reward";
    public static final String XP = "xp";
    public static final String ITEM_DATA = "itemData";
    public static final String ITEM_COUNT = "itemCount";

    private int xp;
    private Map<ItemStack, Integer> items = new HashMap<>();
    private IQuest quest;

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public String getType()
    {
        return Criteria.REWARD;
    }

    @Override
    public boolean respondTo(Event event, @Nullable PlayerEntity player)
    {
        if (event instanceof QuestEvent.Accept) return true; // allow quest to begin with no preconditions
        if (player == null) return false;

        if (event instanceof QuestEvent.Complete) {
            for (ItemStack stack : items.keySet()) {
                int c = items.get(stack);
                if (stack.getMaxStackSize() == 1) {
                    for (int i = 0; i < c; i++) {
                        ItemStack s = stack.copy();
                        PlayerHelper.addOrDropStack(player, s);
                    }
                } else {
                    ItemStack s = stack.copy();
                    s.setCount(c);
                    PlayerHelper.addOrDropStack(player, s);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isSatisfied()
    {
        return true;
    }

    @Override
    public boolean isCompletable()
    {
        return false;
    }

    @Override
    public float getCompletion()
    {
        return 0;
    }

    @Override
    public CompoundNBT toNBT()
    {
        CompoundNBT itemDataTag = new CompoundNBT();
        CompoundNBT itemCountTag = new CompoundNBT();

        if (!items.isEmpty()) {
            int index = 0;
            for (ItemStack stack : items.keySet()) {
                String sindex = Integer.toString(index);
                itemDataTag.put(sindex, stack.serializeNBT());
                itemCountTag.putInt(sindex, items.get(stack));
                index++;
            }
        }

        CompoundNBT tag = new CompoundNBT();
        tag.put(ITEM_DATA, itemDataTag);
        tag.put(ITEM_COUNT, itemCountTag);
        tag.putInt(XP, xp);

        return tag;
    }

    @Override
    public void fromNBT(INBT nbt)
    {
        CompoundNBT data = (CompoundNBT)nbt;
        this.xp = data.getInt(XP);

        CompoundNBT itemDataTag = (CompoundNBT)data.get(ITEM_DATA);
        CompoundNBT itemCountTag = (CompoundNBT)data.get(ITEM_COUNT);

        this.items = new HashMap<>();

        if (itemDataTag != null && itemDataTag.size() > 0) {
            for (int i = 0; i < itemDataTag.size(); i++) {
                String sindex = String.valueOf(i);
                INBT inbt = itemDataTag.get(sindex);
                if (inbt == null) continue;
                ItemStack stack = ItemStack.read((CompoundNBT) inbt);
                int count = Math.max(itemCountTag.getInt(sindex), 1);

                this.items.put(stack, count);
            }
        }
    }

    @Override
    public void setQuest(IQuest quest)
    {
        this.quest = quest;
    }

    @Override
    public boolean shouldRemove()
    {
        return false;
    }

    public Reward addItem(ItemStack stack, int count)
    {
        this.items.put(stack, count);
        return this;
    }

    public Reward setXP(int xp)
    {
        this.xp = xp;
        return this;
    }

    public int getXP()
    {
        return this.xp;
    }

    public Map<ItemStack, Integer> getItems()
    {
        return this.items;
    }
}
