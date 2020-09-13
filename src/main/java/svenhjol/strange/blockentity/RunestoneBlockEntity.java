package svenhjol.strange.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import svenhjol.strange.module.Runestones;

import javax.annotation.Nullable;

public class RunestoneBlockEntity extends BlockEntity {
    public static final String POSITION_TAG = "position";
    public static final String STRUCTURE_TAG = "structure";
    public static final String PLAYER_TAG = "player";

    public BlockPos position;
    public Identifier structure;
    public String player;

    public RunestoneBlockEntity() {
        super(Runestones.BLOCK_ENTITY);
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.position = BlockPos.fromLong(tag.getLong(POSITION_TAG));
        this.player = tag.getString(PLAYER_TAG);

        String structure = tag.getString(STRUCTURE_TAG);
        this.structure = !structure.isEmpty() ? new Identifier(structure) : null;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        if (this.position != null) {
            tag.putLong(POSITION_TAG, this.position.asLong());
            tag.putString(STRUCTURE_TAG, this.structure.toString());
            tag.putString(PLAYER_TAG, this.player);
        }
        return tag;
    }

    @Nullable
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return new BlockEntityUpdateS2CPacket(this.pos, 3, this.toInitialChunkDataTag());
    }
}
