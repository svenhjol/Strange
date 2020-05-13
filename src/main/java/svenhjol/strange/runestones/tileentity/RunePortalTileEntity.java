package svenhjol.strange.runestones.tileentity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import svenhjol.strange.runestones.module.RunePortals;

import javax.annotation.Nullable;

public class RunePortalTileEntity extends TileEntity {
    public BlockPos position;
    public int dimension;
    public int color;
    public int orientation;

    public RunePortalTileEntity() {
        super(RunePortals.tile);
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
        this.position = BlockPos.fromLong(tag.getLong("position"));
        this.dimension = tag.getInt("dimension");
        this.color = tag.getInt("color");
        this.orientation = tag.getInt("orientation");
    }

    protected CompoundNBT writeToNBT(CompoundNBT tag) {
        if (position != null) {
            tag.putLong("position", position.toLong());
        }
        tag.putInt("dimension", dimension);
        tag.putInt("color", color);
        tag.putInt("orientation", orientation);

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
