package svenhjol.strange.runeportals;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;

public class RunePortalBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
    private static final String ORIENTATION_NBT = "orientation";
    private static final String RUNES_NBT = "runes";
    private static final String POS_NBT = "pos";

    public Axis orientation;
    public BlockPos pos;
    public String runes;

    public RunePortalBlockEntity(BlockPos pos, BlockState state) {
        super(RunePortals.RUNE_PORTAL_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.orientation = Axis.fromName(nbt.getString(ORIENTATION_NBT));
        this.runes = nbt.getString(RUNES_NBT);
        this.pos = BlockPos.fromLong(nbt.getLong(POS_NBT));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putString(ORIENTATION_NBT, this.orientation.asString());
        nbt.putString(RUNES_NBT, this.runes);
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
}
