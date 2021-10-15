package svenhjol.strange.module.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class RunestoneBlockEntity extends BlockEntity implements Container, WorldlyContainer, MenuProvider {
    public static final String TAG_MATERIAL = "Material";
    public static final String TAG_RUNES = "Runes";
    public static final String TAG_LOCATION = "Location";
    public static final String TAG_DIFFICULTY = "Difficulty";
    public static final String TAG_DECAY = "Decay";

    public static final int SIZE = 1;
    public static final int[] SLOTS = IntStream.range(0, SIZE).toArray();

    public ResourceLocation location;
    public String runes;
    public int material;
    public float difficulty;
    public float decay;

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private final ContainerData data;

    public RunestoneBlockEntity(BlockPos pos, BlockState state) {
        super(Runestones.BLOCK_ENTITY, pos, state);

        this.items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

        Block block = state.getBlock();
        if (block instanceof RunestoneBlock) {
            this.material = ((RunestoneBlock) block).getMaterial().getId();
        }

        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return RunestoneBlockEntity.this.material;
            }

            @Override
            public void set(int i, int j) {
                RunestoneBlockEntity.this.material = j;
            }

            @Override
            public int getCount() {
                return 1;
            }
        };
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.material = tag.getInt(TAG_MATERIAL);
        this.difficulty = tag.getFloat(TAG_DIFFICULTY);
        this.decay = tag.getFloat(TAG_DECAY);

        String runes = tag.getString(TAG_RUNES);
        this.runes = !runes.isEmpty() ? runes : null;

        String location = tag.getString(TAG_LOCATION);
        this.location = !runes.isEmpty() && !location.isEmpty() ? new ResourceLocation(location) : null;

        ContainerHelper.loadAllItems(tag, this.items);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_MATERIAL, this.material);
        tag.putFloat(TAG_DIFFICULTY, this.difficulty);
        tag.putFloat(TAG_DECAY, this.decay);

        if (this.runes != null) {
            tag.putString(TAG_RUNES, this.runes);
        }

        if (this.location != null && !this.location.toString().isEmpty()) {
            tag.putString(TAG_LOCATION, this.location.toString());
        }

        ContainerHelper.saveAllItems(tag, this.items, false);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slotId, ItemStack itemStack, @Nullable Direction direction) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slotId, ItemStack itemStack, Direction direction) {
        return true;
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    @Override
    public ItemStack getItem(int slotId) {
        return this.items.get(slotId);
    }

    @Override
    public ItemStack removeItem(int slotId, int amount) {
        ItemStack stack = ContainerHelper.removeItem(this.items, slotId, amount);
        if (!stack.isEmpty()) {
            this.setChanged();
        }

        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slotId) {
        return ContainerHelper.takeItem(this.items, slotId);
    }

    @Override
    public void setItem(int slotId, ItemStack stack) {
        this.items.set(slotId, stack);

        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }

        if (level != null && level.isClientSide) {
            // TODO: custom sound effect
            level.playSound(null, getBlockPos(), SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("container.strange.runestone");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new RunestoneMenu(syncId, playerInventory, this, this.data);
    }

//    public void test(Level level) {
//        List<ItemStack> stacks = KnowledgeHelper.generateItemStacksFromBlockPos(level, pos, entity, RunestoneLoot.BASIC);
//        if (stacks.isEmpty() || stacks.size() == 1) {
//            return false;
//        }
//
//        // shift first element as primary
//        ItemStack primary = stacks.remove(0);
//        List<ItemStack> secondary = stacks;
//
//
//    }
}
