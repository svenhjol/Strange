package svenhjol.strange.runestones.tileentity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import svenhjol.strange.runestones.module.Runestones;

import javax.annotation.Nullable;

public class RunestoneTileEntity extends TileEntity {
    public static final String POSITION = "position";
    public static final String DESTINATION = "destination";

    public BlockPos position;
    public String destination;

    public RunestoneTileEntity() {
        super(Runestones.tile);
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        loadFromNBT(tag);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        CompoundNBT nbt = super.write(tag);
        return writeToNBT(nbt);
    }

    protected void loadFromNBT(CompoundNBT tag) {
        this.position = BlockPos.fromLong(tag.getLong(POSITION));
        this.destination = tag.getString(DESTINATION);
    }

    protected CompoundNBT writeToNBT(CompoundNBT tag) {
        if (position != null) {
            tag.putLong(POSITION, position.toLong());
            tag.putString(DESTINATION, destination);
        }
        return tag;
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 3, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        super.onDataPacket(net, pkt);
        handleUpdateTag(pkt.getNbtCompound());
    }
}
