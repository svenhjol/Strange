package svenhjol.strange.runeportals;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

public class RunePortalBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
    private static final String ORIENTATION_NBT = "orientation";
    private static final String COLOR_NBT = "color";
    private static final String RUNES_NBT = "runes";
    private static final String HASH_NBT = "hash";

    public int orientation;
    public int color;
    public long hash;
    public List<Integer> runes;

    public RunePortalBlockEntity(BlockPos pos, BlockState state) {
        super(RunePortals.RUNE_PORTAL_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.orientation = nbt.getInt(ORIENTATION_NBT);
        this.color = nbt.getInt(COLOR_NBT);
        this.hash = nbt.getLong(HASH_NBT);
        this.runes = new ArrayList<>();

        for (int rune : nbt.getIntArray(RUNES_NBT)) {
            this.runes.add(rune);
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt(ORIENTATION_NBT, this.orientation);
        nbt.putInt(COLOR_NBT, this.color);
        nbt.putLong(HASH_NBT, this.hash);
        nbt.putIntArray(RUNES_NBT, this.runes);
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
