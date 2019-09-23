package svenhjol.strange.scrolls.quest.reward;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.strange.scrolls.event.QuestEvent;
import svenhjol.strange.scrolls.quest.Condition;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.Generator.Definition;
import svenhjol.strange.scrolls.quest.iface.ICondition;
import svenhjol.strange.scrolls.quest.iface.IQuest;
import svenhjol.strange.totems.item.TotemOfReturningItem;
import svenhjol.strange.totems.module.TotemOfReturning;
import svenhjol.strange.travelrunes.module.Runestones;
import svenhjol.strange.travelrunes.module.StoneCircles;

import javax.annotation.Nullable;
import java.util.*;

public class RewardItem implements ICondition
{
    public static final String TOTEM_DISTANT_STONE_CIRCLE = "TotemDistantStoneCircle";
    public final static String ID = "RewardItem";
    public static final ArrayList<String> SPECIAL_ITEM_REWARDS = new ArrayList<>(Arrays.asList(
        TOTEM_DISTANT_STONE_CIRCLE
    ));

    private final String STACK = "stack";
    private final String COUNT = "count";

    private IQuest quest;
    private ItemStack stack;
    private int count;
    private World world = null;

    public RewardItem() {}

    public RewardItem(World world)
    {
        this.world = world;
    }

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
    public boolean respondTo(Event event)
    {
        if (event instanceof QuestEvent.Complete) {
            QuestEvent qe = (QuestEvent.Complete)event;
            final PlayerEntity player = qe.getPlayer();
            player.addItemStackToInventory(this.getStack());
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
        CompoundNBT tag = new CompoundNBT();
        tag.put(STACK, stack.serializeNBT());
        tag.putInt(COUNT, count);
        return tag;
    }

    @Override
    public void fromNBT(INBT nbt)
    {
        CompoundNBT data = (CompoundNBT)nbt;
        this.stack = ItemStack.read((CompoundNBT) Objects.requireNonNull(data.get(STACK)));
        this.count = data.getInt(COUNT);
    }

    @Override
    public void setQuest(IQuest quest)
    {
        this.quest = quest;
    }

    public ItemStack getStack()
    {
        return stack;
    }

    public RewardItem setStack(ItemStack stack)
    {
        this.stack = stack;
        return this;
    }

    public int getCount()
    {
        return this.count;
    }

    public RewardItem setCount(int count)
    {
        this.count = count;
        return this;
    }

    @Override
    public List<Condition<RewardItem>> fromDefinition(Definition definition)
    {
        List<Condition<RewardItem>> out = new ArrayList<>();
        Map<String, String> def = definition.getRewardItems();

        for (String key : def.keySet()) {
            ItemStack stack;

            if (SPECIAL_ITEM_REWARDS.contains(key)) {
                stack = getSpecialItemReward(key);
                if (stack == null) continue;
            } else {
                ResourceLocation res = new ResourceLocation(key);
                Item item = ForgeRegistries.ITEMS.getValue(res);
                if (item == null) continue;
                stack = new ItemStack(item);
            }

            int count = definition.parseCount(def.get(key));

            Condition<RewardItem> condition = Condition.factory(RewardItem.class, quest);
            condition.getDelegate().setStack(stack).setCount(count);
            out.add(condition);
        }

        return out;
    }

    @Nullable
    private ItemStack getSpecialItemReward(String item)
    {
        switch (item) {
            case TOTEM_DISTANT_STONE_CIRCLE:
                if (world == null) return null;
                ItemStack totem = new ItemStack(TotemOfReturning.item);
                final BlockPos circlePos = world.findNearestStructure(StoneCircles.NAME, Runestones.getOuterPos(world.rand), 1000, true);

                if (circlePos != null) {
                    TotemOfReturningItem.setPos(totem, circlePos.add(0, 1, 0));
                    totem.setDisplayName(new StringTextComponent("Distant Stone Circle"));
                }

                return totem;

            default:
                break;
        }

        return null;
    }
}
