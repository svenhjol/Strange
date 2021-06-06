package svenhjol.strange.module.runestones;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RunestoneBlockEntity extends BlockEntity {
    public static final String POSITION_TAG = "position";
    public static final String LOCATION_TAG = "location";
    public static final String PLAYER_TAG = "player";

    public BlockPos position;
    public ResourceLocation location;
    public String player;

    public RunestoneBlockEntity(BlockPos pos, BlockState state) {
        super(Runestones.BLOCK_ENTITY, pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.position = BlockPos.of(tag.getLong(POSITION_TAG));
        this.player = tag.getString(PLAYER_TAG);

        String location = tag.getString(LOCATION_TAG);
        this.location = !location.isEmpty() ? new ResourceLocation(location) : null;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        super.save(tag);
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
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 3, this.getUpdateTag());
    }
}
