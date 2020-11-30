package svenhjol.strange.runicaltars;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import svenhjol.strange.runicfragments.RunicFragmentItem;
import svenhjol.strange.runestones.RunestonesHelper;
import svenhjol.strange.runicfragments.RunicFragments;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class RunicAltarBlockEntity extends BlockEntity implements Inventory, SidedInventory, NamedScreenHandlerFactory, BlockEntityClientSerializable {
    public static final String DESTINATION_TAG = "Destination";
    public static int SIZE = 1;
    private static final int[] SLOTS = IntStream.range(0, SIZE).toArray();
    private DefaultedList<ItemStack> items = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);
    private BlockPos destination;

    public RunicAltarBlockEntity() {
        super(RunicAltars.BLOCK_ENTITY);
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.items = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);
        Inventories.fromTag(tag, this.items);

        if (tag.contains(DESTINATION_TAG))
            this.destination = BlockPos.fromLong(tag.getLong(DESTINATION_TAG));
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        Inventories.toTag(tag, this.items, false);

        if (destination != null)
            tag.putLong(DESTINATION_TAG, destination.asLong());

        return tag;
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack itemStack = Inventories.splitStack(this.items, slot, amount);
        if (!itemStack.isEmpty()) {
            this.markDirty();
        }

        return itemStack;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.items, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.items.set(slot, stack);

        if (stack.getCount() > this.getMaxCountPerStack())
            stack.setCount(this.getMaxCountPerStack());

        if (!world.isClient)
            world.playSound(null, pos, SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.BLOCKS, 1.0F, 1.0F);

        trySetDestination(stack);

        this.markDirty();
        this.sync();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("container.strange.runic_altar");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new RunicAltarScreenHandler(syncId, inv, this, ScreenHandlerContext.create(world, pos));
    }

    @Override
    public void clear() {
        this.items.clear();
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return true;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    public void fromClientTag(CompoundTag compoundTag) {
        fromTag(null, compoundTag);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag compoundTag) {
        return toTag(compoundTag);
    }

    @Nullable
    public BlockPos getDestination() {
        return this.destination;
    }

    private void trySetDestination(ItemStack stack) {
        if (stack.getItem() == RunicFragments.RUNIC_FRAGMENT) {
            if (!RunicFragmentItem.isPopulated(stack) && !RunicFragmentItem.populate(stack, world, pos, world.random))
                return;

            this.destination = RunicFragmentItem.getNormalizedPos(stack, world);
        } else {
            this.destination = RunestonesHelper.getBlockPosFromItemStack(world, stack);
        }
    }
}
