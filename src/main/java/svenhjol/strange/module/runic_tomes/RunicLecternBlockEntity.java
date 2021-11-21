package svenhjol.strange.module.runic_tomes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RunicLecternBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final String TAG_TOME = "Tome";

    public static final int SIZE = 1;

    protected final NonNullList<ItemStack> items;
    protected ItemStack tome;

    public RunicLecternBlockEntity(BlockPos pos, BlockState state) {
        super(RunicTomes.RUNIC_LECTERN_BLOCK_ENTITY, pos, state);
        this.items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        this.tome = ItemStack.EMPTY;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);

        CompoundTag tomeTag = tag.getCompound(TAG_TOME);
        tome = ItemStack.of(tomeTag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.items, false);

        CompoundTag tomeTag = new CompoundTag();
        tome.save(tomeTag);

        tag.put(TAG_TOME, tomeTag);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("container.strange.runic_lectern");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new RunicLecternMenu(i, inventory, this, ContainerLevelAccess.create(player.getLevel(), getBlockPos()));
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
    public ItemStack getItem(int slotIndex) {
        return items.get(slotIndex);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        ItemStack stack = ContainerHelper.removeItem(items, i, j);
        if (stack.isEmpty()) {
            setChanged();
        }

        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ContainerHelper.takeItem(items, i);
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        items.set(i, itemStack);

        if (itemStack.getCount() > getMaxStackSize()) {
            itemStack.setCount(getMaxStackSize());
        }

        setChanged();
    }

    public void setTome(ItemStack stack) {
        ItemStack copy = stack.copy();

        if (copy.getCount() > 1) {
            copy.setCount(1);
        }
        
        tome = copy;
        setChanged();
    }

    public ItemStack getTome() {
        return tome;
    }

    public void clearTome() {
        tome = ItemStack.EMPTY;
    }

    public boolean hasTome() {
        return tome != null && !tome.isEmpty();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
