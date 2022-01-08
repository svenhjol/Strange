package svenhjol.strange.module.chairs;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StairBlock;
import svenhjol.charm.helper.LogHelper;

public class ChairEntity extends Entity {
    public ChairEntity(EntityType<? extends ChairEntity> type, Level level) {
        super(type, level);
    }

    public ChairEntity(Level level, BlockPos pos) {
        super(Chairs.CHAIR, level);
        setPos(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }

    @Override
    protected void defineSynchedData() {
        // no
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        // nope
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        // nah
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public double getPassengersRidingOffset() {
        return -0.25F;
    }

    @Override
    public void tick() {
        super.tick();
        var pos = blockPosition();
        var state = level.getBlockState(pos);
        var block = state.getBlock();

        if (!level.isClientSide) {
            var stateAbove = level.getBlockState(pos.above());

            if (!(block instanceof StairBlock)) {

                unRide();
                remove(RemovalReason.DISCARDED);
                LogHelper.debug(getClass(), "Removing because no longer a stairs block");

            } else if (stateAbove.isCollisionShapeFullBlock(level, pos.above())) {

                unRide();
                remove(RemovalReason.DISCARDED);
                LogHelper.debug(getClass(), "Removing because block above is invalid");

            } else if (!hasExactlyOnePlayerPassenger()) {

                remove(RemovalReason.DISCARDED);
                LogHelper.debug(getClass(), "Removing because no passengers");

            }
        }

        if (!isRemoved() && block instanceof StairBlock && !Chairs.allowRotation) {
            var passenger = getFirstPassenger();
            if (passenger instanceof Player player) {
                var facing = state.getValue(StairBlock.FACING).getOpposite();
                player.setYRot(facing.toYRot());
            }
        }
    }
}
