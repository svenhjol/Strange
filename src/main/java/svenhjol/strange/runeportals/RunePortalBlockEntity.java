package svenhjol.strange.runeportals;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class RunePortalBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
    private static final String ORIENTATION_NBT = "orientation";
    private static final String COLOR_NBT = "color";

    public int orientation;
    public int color;

    public RunePortalBlockEntity(BlockPos pos, BlockState state) {
        super(RunePortals.RUNE_PORTAL_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.orientation = nbt.getInt(ORIENTATION_NBT);
        this.color = nbt.getInt(COLOR_NBT);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt(ORIENTATION_NBT, this.orientation);
        nbt.putInt(COLOR_NBT, this.color);
        return nbt;
    }

    @Override
    public void fromClientTag(NbtCompound nbt) {
        readNbt(nbt);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound nbtCompound) {
        return writeNbt(nbtCompound);
    }

    @Environment(EnvType.CLIENT)
    public boolean shouldDrawSide(Direction face) {
        if (this.orientation == 0) {
            return face == Direction.NORTH || face == Direction.SOUTH;
        } else {
            return face == Direction.EAST || face == Direction.WEST;
        }
    }
}
