package svenhjol.strange.scrolls.quest.condition;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.strange.base.QuestHelper;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.iface.IDelegate;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import java.util.Objects;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Mine implements IDelegate
{
    public final static String ID = "Mine";

    private IQuest quest;
    private Block block;
    private int count;
    private int mined;

    private final String BLOCK = "block";
    private final String COUNT = "count";
    private final String MINED = "mined";

    @Override
    public String getType()
    {
        return Criteria.ACTION;
    }

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public boolean respondTo(Event event)
    {
        if (isSatisfied()) return false;
        if (mined >= count) return false;

//        if (event instanceof HarvestDropsEvent) {
//            HarvestDropsEvent blockEvent = (HarvestDropsEvent)event;
//            BlockState state = blockEvent.getWorld().getBlockState(blockEvent.getPos());
//            Block block = state.getBlock();
//            ResourceLocation blockRes = block.getRegistryName();
//
//            if (this.block == null || blockRes == null || !blockRes.equals(this.block.getRegistryName())) return false;
//
//            PlayerEntity player = blockEvent.getHarvester();
//            if (player == null) return false;
//
//            int count = 1;
//            int remaining = getRemaining();
//
//            if (count > remaining || remaining - 1 == 0) {
//                // set the count to the remainder
//                count = remaining;
//            } else {
//                blockEvent.setDropChance(0.0F);
//            }
//
//            mined += count;
//
//            if (isSatisfied()) {
//                Quests.effectCompleted(player, new TranslationTextComponent("event.strange.quests.mined_all"));
//            } else {
//                Quests.effectCounted(player);
//            }
//
//            return true;
//        }

        if (event instanceof BreakEvent) {
            BreakEvent blockEvent = (BreakEvent)event;
            BlockPos blockPos = blockEvent.getPos();
            BlockState state = blockEvent.getWorld().getBlockState(blockEvent.getPos());
            Block block = state.getBlock();
            ResourceLocation blockRes = block.getRegistryName();

            if (this.block == null || blockRes == null || !blockRes.equals(this.block.getRegistryName())) return false;

            PlayerEntity player = blockEvent.getPlayer();
            World world = (World)blockEvent.getWorld();

            int count = 1;
            int remaining = getRemaining();

            if (count > remaining || remaining - 1 == 0) {
                // set the count to the remainder
                count = remaining;
            }

            blockEvent.setCanceled(true);
            world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 2);

            mined += count;

            if (isSatisfied()) {
                QuestHelper.effectCompleted(player, new TranslationTextComponent("event.strange.quests.mined_all"));
            } else {
                QuestHelper.effectCounted(player);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean isSatisfied()
    {
        return count <= mined;
    }

    @Override
    public boolean isCompletable()
    {
        return true;
    }

    @Override
    public float getCompletion()
    {
        int collected = Math.min(this.mined, this.count);
        if (collected == 0 || count == 0) return 0;
        return ((float)collected / (float)count) * 100;
    }

    @Override
    public CompoundNBT toNBT()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putString(BLOCK, Objects.requireNonNull(block.getRegistryName()).toString());
        tag.putInt(MINED, mined);
        tag.putInt(COUNT, count);
        return tag;
    }

    @Override
    public void fromNBT(INBT nbt)
    {
        CompoundNBT data = (CompoundNBT)nbt;
        this.block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(data.getString(BLOCK)));
        this.count = data.getInt(COUNT);
        this.mined = data.getInt(MINED);
    }

    @Override
    public void setQuest(IQuest quest)
    {
        this.quest = quest;
    }

    public Mine setCount(int count)
    {
        this.count = count;
        return this;
    }

    public Mine setBlock(Block block)
    {
        this.block = block;
        return this;
    }

    public int getMined()
    {
        return this.mined;
    }

    public int getCount()
    {
        return this.count;
    }

    public int getRemaining()
    {
        return count - mined;
    }

    public Block getBlock()
    {
        return this.block;
    }
}
