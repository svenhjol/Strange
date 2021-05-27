package svenhjol.strange.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import svenhjol.strange.module.Runestones;

import javax.annotation.Nullable;

public class RunestoneBlockEntity extends BlockEntity {
    public static final String POSITION_TAG = "position";
    public static final String LOCATION_TAG = "location";
    public static final String PLAYER_TAG = "player";

    public BlockPos position;
    public Identifier location;
    public String player;

    public RunestoneBlockEntity(BlockPos pos, BlockState state) {
        super(Runestones.BLOCK_ENTITY, pos, state);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        this.position = BlockPos.fromLong(tag.getLong(POSITION_TAG));
        this.player = tag.getString(PLAYER_TAG);

        String location = tag.getString(LOCATION_TAG);
        this.location = !location.isEmpty() ? new Identifier(location) : null;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        if (this.position != null) {
            tag.putLong(POSITION_TAG, this.position.asLong());

            if (location != null)
                tag.putString(LOCATION_TAG, this.location.toString());

            if (player != null)
                tag.putString(PLAYER_TAG, this.player);
        }
        return tag;
    }

    @Nullable
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return new BlockEntityUpdateS2CPacket(this.pos, 3, this.toInitialChunkDataNbt());
    }
}
