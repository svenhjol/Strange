package svenhjol.strange.module.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class RunestoneBlockEntity extends BlockEntity {
    public static final String POSITION_NBT = "Position";
    public static final String LOCATION_NBT = "Location";
    public static final String PLAYER_NBT = "Player";

    public BlockPos position;
    public ResourceLocation location;
    public String player;

    public RunestoneBlockEntity(BlockPos pos, BlockState state) {
        super(Runestones.BLOCK_ENTITY, pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.position = BlockPos.of(tag.getLong(POSITION_NBT));
        this.player = tag.getString(PLAYER_NBT);

        String location = tag.getString(LOCATION_NBT);
        this.location = !location.isEmpty() ? new ResourceLocation(location) : null;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        super.save(tag);
        if (this.position != null) {
            tag.putLong(POSITION_NBT, this.position.asLong());

            if (location != null)
                tag.putString(LOCATION_NBT, this.location.toString());

            if (player != null)
                tag.putString(PLAYER_NBT, this.player);
        }
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 3, this.getUpdateTag());
    }
}
