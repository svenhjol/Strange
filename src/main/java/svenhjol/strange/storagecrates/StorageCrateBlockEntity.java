package svenhjol.strange.storagecrates;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public class StorageCrateBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
    public static final String ITEM_NBT = "Item";
    public static final String COUNT_NBT = "Count";

    public Item item;
    public int count;

    public StorageCrateBlockEntity(BlockPos pos, BlockState state) {
        super(StorageCrates.BLOCK_ENTITY, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        Identifier itemId = new Identifier(nbt.getString(ITEM_NBT));
        this.item = Registry.ITEM.getOrEmpty(itemId).orElse(null);
        this.count = nbt.getInt(COUNT_NBT);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        if (this.item != null) {
            String itemId = Registry.ITEM.getId(this.item).toString();
            nbt.putString(ITEM_NBT, itemId);
        }

        nbt.putInt(COUNT_NBT, this.count);

        return nbt;
    }

    @Override
    public void fromClientTag(NbtCompound nbt) {
        readNbt(nbt);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound nbt) {
        return writeNbt(nbt);
    }
}
