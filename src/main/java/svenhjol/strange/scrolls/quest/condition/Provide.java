package svenhjol.strange.scrolls.quest.condition;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;
import svenhjol.strange.base.helper.QuestHelper;
import svenhjol.strange.scrolls.event.QuestEvent;
import svenhjol.strange.scrolls.quest.Criteria;
import svenhjol.strange.scrolls.quest.iface.IDelegate;
import svenhjol.strange.scrolls.quest.iface.IQuest;

import javax.annotation.Nullable;
import java.util.Objects;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Provide implements IDelegate {
    public final static String ID = "Provide";

    private IQuest quest;
    private ItemStack stack;
    private int count;
    private boolean remove = false;

    private final String STACK = "stack";
    private final String COUNT = "count";

    @Override
    public boolean isSatisfied() {
        return true;
    }

    @Override
    public boolean isCompletable() {
        return false;
    }

    @Override
    public float getCompletion() {
        return 0;
    }

    @Override
    public boolean respondTo(Event event, @Nullable PlayerEntity player) {
        if (player == null) return false;

        World world = player.world;

        if (event instanceof QuestEvent.Accept) {
            return this.onStarted(player);
        }

        return false;
    }

    @Override
    public String getType() {
        return Criteria.CONSTRAINT;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public CompoundNBT toNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.put(STACK, stack.serializeNBT());
        tag.putInt(COUNT, count);
        return tag;
    }

    @Override
    public void fromNBT(INBT nbt) {
        CompoundNBT data = (CompoundNBT) nbt;
        this.stack = ItemStack.read((CompoundNBT) Objects.requireNonNull(data.get(STACK)));
        this.count = data.getInt(COUNT);
    }

    @Override
    public void setQuest(IQuest quest) {
        this.quest = quest;
    }

    @Override
    public boolean shouldRemove() {
        return this.remove;
    }

    public Provide setCount(int count) {
        this.count = count;
        return this;
    }

    public Provide setStack(ItemStack stack) {
        this.stack = stack;
        return this;
    }

    public int getCount() {
        return count;
    }

    public ItemStack getStack() {
        return stack;
    }

    public boolean onStarted(PlayerEntity player) {
        ItemStack give = this.stack.copy();
        give.setCount(this.count);
        QuestHelper.giveQuestItemToPlayer(player, give);
        this.remove = true;
        return true;
    }
}
