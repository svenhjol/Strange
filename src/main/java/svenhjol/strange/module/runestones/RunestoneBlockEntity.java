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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RunestoneBlockEntity extends BlockEntity implements Container, WorldlyContainer, MenuProvider {
    public static final String RUNES_NBT = "Runes";
    public static final String POSITION_NBT = "Position";
    public static final String LOCATION_NBT = "Location";
    public static final String PLAYER_NBT = "Player";
    public static final String MATERIAL_NBT = "Material";

    public static final int SIZE = 1;
    public static final int[] SLOTS = IntStream.range(0, SIZE).toArray();

    public List<Integer> runes;
    public BlockPos position;
    public ResourceLocation location;
    public String player;
    public int material;

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
        this.runes = Arrays.stream(tag.getIntArray(RUNES_NBT)).boxed().collect(Collectors.toList());
        this.position = BlockPos.of(tag.getLong(POSITION_NBT));
        this.player = tag.getString(PLAYER_NBT);
        this.material = tag.getInt(MATERIAL_NBT);

        String location = tag.getString(LOCATION_NBT);
        this.location = !location.isEmpty() ? new ResourceLocation(location) : null;

        ContainerHelper.loadAllItems(tag, this.items);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(MATERIAL_NBT, this.material);
        ContainerHelper.saveAllItems(tag, this.items, false);

        if (this.runes != null) {
            tag.putIntArray(RUNES_NBT, this.runes);
        }

        if (this.position != null) {
            tag.putLong(POSITION_NBT, this.position.asLong());

            if (location != null)
                tag.putString(LOCATION_NBT, this.location.toString());

            if (player != null)
                tag.putString(PLAYER_NBT, this.player);
        }
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
            level.playSound(null, position, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.BLOCKS, 1.0F, 1.0F);
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
}
