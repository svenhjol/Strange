package svenhjol.strange.module.rune_portals;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RunePortalBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
    private static final String ORIENTATION_NBT = "Orientation";
    private static final String RUNES_NBT = "Runes";
    private static final String POS_NBT = "Pos";

    public Axis orientation;
    public BlockPos pos;
    public String runes;

    public RunePortalBlockEntity(BlockPos pos, BlockState state) {
        super(RunePortals.RUNE_PORTAL_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.orientation = Axis.byName(nbt.getString(ORIENTATION_NBT));
        this.runes = nbt.getString(RUNES_NBT);
        this.pos = BlockPos.of(nbt.getLong(POS_NBT));
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        super.save(nbt);
        nbt.putString(ORIENTATION_NBT, this.orientation.getSerializedName());
        nbt.putString(RUNES_NBT, this.runes);
        nbt.putLong(POS_NBT, this.pos.asLong());
        return nbt;
    }

    @Override
    public void fromClientTag(CompoundTag nbt) {
        load(nbt);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag nbtCompound) {
        return save(nbtCompound);
    }
}
