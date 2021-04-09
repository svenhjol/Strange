package svenhjol.strange.runeportals;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RunePortalBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
    private static final String ORIENTATION_NBT = "orientation";
    private static final String COLOR_NBT = "color";
    private static final String RUNES_NBT = "runes";
    private static final String POS_NBT = "pos";

    public Axis orientation;
    public DyeColor color;
    public BlockPos pos;
    public List<Integer> runes;

    public RunePortalBlockEntity(BlockPos pos, BlockState state) {
        super(RunePortals.RUNE_PORTAL_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.orientation = Axis.fromName(nbt.getString(ORIENTATION_NBT));
        this.color = DyeColor.byId(nbt.getInt(COLOR_NBT));
        this.runes = Arrays.stream(nbt.getIntArray(RUNES_NBT)).boxed().collect(Collectors.toList());
        this.pos = BlockPos.fromLong(nbt.getLong(POS_NBT));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putString(ORIENTATION_NBT, this.orientation.asString());
        nbt.putInt(COLOR_NBT, this.color.getId());
        nbt.putIntArray(RUNES_NBT, this.runes);
        nbt.putLong(POS_NBT, this.pos.asLong());
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
        if (this.orientation == Axis.Z) {
            return face == Direction.NORTH || face == Direction.SOUTH;
        } else {
            return face == Direction.EAST || face == Direction.WEST;
        }
    }
}
