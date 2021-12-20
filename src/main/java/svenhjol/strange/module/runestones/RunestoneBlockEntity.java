package svenhjol.strange.module.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charm.block.CharmSyncedBlockEntity;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class RunestoneBlockEntity extends CharmSyncedBlockEntity implements Container, WorldlyContainer, MenuProvider {
    public static final String MATERIAL_TAG = "Material";
    public static final String RUNES_TAG = "Runes";
    public static final String LOCATION_TAG = "Location";
    public static final String DIFFICULTY_TAG = "Difficulty";

    public static final int SIZE = 1;
    public static final int[] SLOTS = IntStream.range(0, SIZE).toArray();

    public @Nullable ResourceLocation location;
    public String runes;
    public int material;
    public float difficulty;

    private final NonNullList<ItemStack> items;
    private final ContainerData data;

    public RunestoneBlockEntity(BlockPos pos, BlockState state) {
        super(Runestones.BLOCK_ENTITY, pos, state);

        this.items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

        Block block = state.getBlock();
        if (block instanceof RunestoneBlock) {
            this.material = ((RunestoneBlock) block).getMaterial().ordinal();
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
        this.material = tag.getInt(MATERIAL_TAG);
        this.difficulty = tag.getFloat(DIFFICULTY_TAG);

        String runes = tag.getString(RUNES_TAG);
        this.runes = !runes.isEmpty() ? runes : null;

        String location = tag.getString(LOCATION_TAG);
        this.location = !runes.isEmpty() && !location.isEmpty() ? new ResourceLocation(location) : null;

        ContainerHelper.loadAllItems(tag, this.items);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(MATERIAL_TAG, material);
        tag.putFloat(DIFFICULTY_TAG, difficulty);

        if (runes != null) {
            tag.putString(RUNES_TAG, runes);
        }

        if (location != null && !location.toString().isEmpty()) {
            tag.putString(LOCATION_TAG, location.toString());
        }

        ContainerHelper.saveAllItems(tag, items, false);
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
        return items.isEmpty();
    }

    @Override
    public ItemStack getItem(int slotId) {
        return items.get(slotId);
    }

    @Override
    public ItemStack removeItem(int slotId, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slotId, amount);

        if (!stack.isEmpty()) {
            setChanged();
        }

        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slotId) {
        return ContainerHelper.takeItem(this.items, slotId);
    }

    @Override
    public void setItem(int slotId, ItemStack stack) {
        items.set(slotId, stack);

        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }

        if (level != null && level.isClientSide) {
            // TODO: custom sound effect
            level.playSound(null, getBlockPos(), SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        setChanged();
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
        return new RunestoneMenu(syncId, playerInventory, this, data, ContainerLevelAccess.create(player.getLevel(), getBlockPos()));
    }

    public String getRunes() {
        return runes;
    }
}
