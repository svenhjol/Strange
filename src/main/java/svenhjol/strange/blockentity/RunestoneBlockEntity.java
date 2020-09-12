package svenhjol.strange.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import svenhjol.strange.module.Runestones;

import javax.annotation.Nullable;

public class RunestoneBlockEntity extends BlockEntity {
    public static final String POSITION_TAG = "position";
    public static final String STRUCTURE_TAG = "structure";
    public static final String DESCRIPTION_TAG = "description";

    public BlockPos position;
    public String structure;
    public String description;

    public RunestoneBlockEntity() {
        super(Runestones.BLOCK_ENTITY);
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.position = BlockPos.fromLong(tag.getLong(POSITION_TAG));
        this.structure = tag.getString(STRUCTURE_TAG);
        this.description = tag.getString(DESCRIPTION_TAG);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        if (this.position != null) {
            tag.putLong(POSITION_TAG, this.position.asLong());
            tag.putString(STRUCTURE_TAG, this.structure);
            tag.putString(DESCRIPTION_TAG, this.description);
        }
        return tag;
    }

    @Nullable
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return new BlockEntityUpdateS2CPacket(this.pos, 3, this.toInitialChunkDataTag());
    }
}
